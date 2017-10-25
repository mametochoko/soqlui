package com.gmail.chibitopoochan.soqlui.controller.service;

import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gmail.chibitopoochan.soqlui.logic.ConnectionLogic;
import com.gmail.chibitopoochan.soqlui.util.FormatUtils;
import com.gmail.chibitopoochan.soqlui.util.format.CSVFormatDecoration;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ExportService extends Service<Void> {
	// クラス共通の参照
	private static final Logger logger = LoggerFactory.getLogger(SOQLExecuteService.class);

	public static final int DEFAULT_BATCH_SIZE = 1000;
	public static final int MAX_BATCH_SIZE = 2000;

	/**
	 * 接続ロジックのプロパティ.
	 * Salesforce接続の設定
	 */
	private ObjectProperty<ConnectionLogic> connectionLogic = new SimpleObjectProperty<ConnectionLogic>(this, "connectionLogic");
	public void setConnectionLogic(ConnectionLogic logic) {
		connectionLogic.set(logic);
	}

	public ConnectionLogic getConnectionLogic() {
		return connectionLogic.get();
	}

	public ObjectProperty<ConnectionLogic> connectionLogicProperty() {
		return connectionLogic;
	}

	/**
	 * SOQLプロパティ.
	 * 実行するSOQLの設定
	 */
	private StringProperty soql = new SimpleStringProperty(this, "soql");
	public void setSOQL(String query) {
		soql.set(query);
	}

	public String getSOQL() {
		return soql.get();
	}

	public StringProperty soqlProperty() {
		return soql;
	}

	/**
	 * ALLオプションのプロパティ.
	 * 削除済みレコードも取得するかの設定
	 */
	private BooleanProperty all = new SimpleBooleanProperty(this, "all");
	public void setAll(boolean getAll) {
		all.set(getAll);
	}

	public boolean isAll() {
		return all.get();
	}

	public BooleanProperty allProperty() {
		return all;
	}

	/**
	 * バッチサイズのプロパティ.
	 * 一度の実行で取得できるサイズ
	 */
	private StringProperty batchSize = new SimpleStringProperty(this, "batchSize");
	public void setBatchSize(String size) {
		batchSize.set(size);
	}

	public String getBatchSize() {
		return batchSize.get();
	}

	public StringProperty batchSizeProperty() {
		return batchSize;
	}

	public int getIntBatchSize() {
		int size = DEFAULT_BATCH_SIZE;

		try {
			size = Integer.parseInt(getBatchSize());
		} catch (Exception e) {
			logger.error("Invalid batch size:" + getBatchSize(), e);
		}

		if(size > MAX_BATCH_SIZE) {
			size = MAX_BATCH_SIZE;
		}

		return size;
	}

	private Path exportPath;

	public void setExportPath(Path path) {
		this.exportPath = path;
	}

	public Path getExportPath() {
		return exportPath;
	}

	@Override
	protected Task<Void> createTask() {
		final ConnectionLogic useLogic = connectionLogic.get();
		final String useSOQL = getSOQL();
		final boolean useAll = isAll();
		final int useBatchSize = getIntBatchSize();

		return new Task<Void>(){

			@Override
			protected Void call() throws Exception {
				try(BufferedWriter out = Files.newBufferedWriter(exportPath, Charset.forName("UTF-8"))) {
					List<List<String>> rowList = new LinkedList<>();

					updateMessage("SOQL Executing...");
					List<Map<String, String>> recordList = useLogic.execute(useSOQL, useAll, useBatchSize);
					int size = useLogic.getSize();
					int done = 0;

					if(!recordList.isEmpty()) {
						rowList.add(recordList.get(0).keySet().stream().collect(Collectors.toList()));
					}

					while(!recordList.isEmpty()) {
						recordList.forEach(record -> {
							rowList.add(record.values().stream().collect(Collectors.toList()));
						});

						String csv = FormatUtils.format(new CSVFormatDecoration(), () -> rowList);
						out.write(csv);

						done += recordList.size();

						updateProgress(done, size);
						updateMessage(String.format("%d / %d", done, size));

						rowList.clear();
						recordList = useLogic.executeMore();
					}

					out.flush();
					updateMessage("Exported " + size);
				}

				return null;
			}

		};
	}

}
