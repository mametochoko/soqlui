package com.gmail.chibitopoochan.soqlui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gmail.chibitopoochan.soqlui.SceneManager;
import com.gmail.chibitopoochan.soqlui.controller.initialize.MainInitializer;
import com.gmail.chibitopoochan.soqlui.controller.service.ConnectService;
import com.gmail.chibitopoochan.soqlui.controller.service.FieldProvideService;
import com.gmail.chibitopoochan.soqlui.controller.service.SOQLExecuteService;
import com.gmail.chibitopoochan.soqlui.logic.ConnectionSettingLogic;
import com.gmail.chibitopoochan.soqlui.model.DescribeField;
import com.gmail.chibitopoochan.soqlui.model.DescribeSObject;
import com.gmail.chibitopoochan.soqlui.model.SObjectRecord;
import com.gmail.chibitopoochan.soqlui.util.Constants.Configuration;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class MainController implements Initializable, Controller {
	// クラス共通の参照
	private static final Logger logger = LoggerFactory.getLogger(MainController.class);

	// 画面上のコンポーネント
	// メニュー
	@FXML private MenuItem menuFileConnection;

	// 左側上段
	@FXML private ComboBox<String> connectOption;
	@FXML private Button connect;
	@FXML private Button disconnect;
	@FXML private ProgressIndicator progressIndicator;

	// 左側中断
	@FXML private TableView<DescribeSObject> sObjectList;
	@FXML private TableColumn<DescribeSObject, String> prefixColumn;
	@FXML private TableColumn<DescribeSObject, String> sObjectColumn;
	@FXML private TextField objectSearch;

	// 左側下段
	@FXML private TableView<DescribeField> fieldList;
	@FXML private TextField columnSearch;

	// 中央
	@FXML private Button execute;
	@FXML private TextArea soqlArea;
	@FXML private TextField batchSize;
	@FXML private CheckBox all;
	@FXML private TableView<SObjectRecord> resultTable;

	// 業務ロジック
	private SceneManager manager;
	private ConnectionSettingLogic setting = new ConnectionSettingLogic();

	// 非同期のサービス
	private ConnectService connectService = new ConnectService();
	private SOQLExecuteService executionService = new SOQLExecuteService();
	private FieldProvideService fieldService = new FieldProvideService();

	// 状態管理
	private ObservableList<DescribeSObject> objectMasterList = FXCollections.observableArrayList();
	private ObservableList<DescribeField> fieldMasterList = FXCollections.observableArrayList();

	// 初期化
	private MainInitializer init;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.manager = SceneManager.getInstance();

		// 画面の初期化
		init = new MainInitializer();
		init.setController(this);
		init.initialize();

	}

	/**
	 * 接続イベント
	 */
	public void doConnect() {
		// 変数をバインド
		progressIndicator.progressProperty().unbind();
		progressIndicator.visibleProperty().unbind();
		progressIndicator.progressProperty().bind(connectService.progressProperty());
		progressIndicator.visibleProperty().bind(connectService.runningProperty());

		connect.setDisable(true);

		// 接続を開始
		String selected = connectOption.getValue();
		connectService.setConnectionSetting(setting.getConnectionSetting(selected));
		connectService.setClosing(false);
		connectService.start();

	}

	/**
	 * 切断イベント
	 */
	public void doDisconnect() {
		// 変数をバインド
		progressIndicator.progressProperty().unbind();
		progressIndicator.visibleProperty().unbind();
		progressIndicator.progressProperty().bind(connectService.progressProperty());
		progressIndicator.visibleProperty().bind(connectService.runningProperty());

		// 切断を開始
		connectService.setClosing(true);
		connectService.start();

	}

	/**
	 * SOQLを実行
	 */
	public void doExecute() {
		// TODO オプションは後程設定
		executionService.start();

	}

	/**
	 * 接続設定画面の表示
	 */
	public void openConnectSetting() {
		try {
			logger.debug("Open Window [Connection Setting]");
			manager.sceneOpen(Configuration.VIEW_SU02, Configuration.TITLE_SU02);
		} catch (IOException e) {
			logger.error("Open window error", e);
		}
	}

	/**
	 * 接続情報を更新
	 */
	@Override
	public void onCloseChild() {
		init.reset();
	}

	/**
	 * @return connectOption
	 */
	public ComboBox<String> getConnectOption() {
		return connectOption;
	}

	/**
	 * @return connect
	 */
	public Button getConnect() {
		return connect;
	}

	/**
	 * @return disconnect
	 */
	public Button getDisconnect() {
		return disconnect;
	}

	/**
	 * @return progressIndicator
	 */
	public ProgressIndicator getProgressIndicator() {
		return progressIndicator;
	}

	/**
	 * @return sObjectList
	 */
	public TableView<DescribeSObject> getsObjectList() {
		return sObjectList;
	}

	/**
	 * @return prefixColumn
	 */
	public TableColumn<DescribeSObject, String> getPrefixColumn() {
		return prefixColumn;
	}

	/**
	 * @return sObjectColumn
	 */
	public TableColumn<DescribeSObject, String> getsObjectColumn() {
		return sObjectColumn;
	}

	/**
	 * @return objectSearch
	 */
	public TextField getObjectSearch() {
		return objectSearch;
	}

	/**
	 * @return fieldList
	 */
	public TableView<DescribeField> getFieldList() {
		return fieldList;
	}

	/**
	 * @return columnSearch
	 */
	public TextField getColumnSearch() {
		return columnSearch;
	}

	/**
	 * @return execute
	 */
	public Button getExecute() {
		return execute;
	}

	/**
	 * @return soqlArea
	 */
	public TextArea getSoqlArea() {
		return soqlArea;
	}

	/**
	 * @return batchSize
	 */
	public TextField getBatchSize() {
		return batchSize;
	}

	/**
	 * @return all
	 */
	public CheckBox getAll() {
		return all;
	}

	/**
	 * @return resultTable
	 */
	public TableView<SObjectRecord> getResultTable() {
		return resultTable;
	}

	/**
	 * @return setting
	 */
	public ConnectionSettingLogic getSetting() {
		return setting;
	}

	/**
	 * @return connectService
	 */
	public ConnectService getConnectService() {
		return connectService;
	}

	/**
	 * @return executionService
	 */
	public SOQLExecuteService getExecutionService() {
		return executionService;
	}

	/**
	 * @return fieldService
	 */
	public FieldProvideService getFieldService() {
		return fieldService;
	}

	/**
	 * @return objectMasterList
	 */
	public ObservableList<DescribeSObject> getObjectMasterList() {
		return objectMasterList;
	}

	/**
	 * @return fieldMasterList
	 */
	public ObservableList<DescribeField> getFieldMasterList() {
		return fieldMasterList;
	}

}
