package application;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class GarbageController implements Initializable{
	List<String> list = new ArrayList<String>();
	Connection con;
	PreparedStatement pstmt;
	ResultSet rs;
	String sql, bar_name;
	int bar_id = 0;
	ObservableList<StoreBean> storeList;
	
	@FXML
	Alert alert;
	@FXML
	private ComboBox<String> barCom;
	@FXML
	private Button searchBtn, excelBtn;
	@FXML
	private TableView<StoreBean> storeTable;
	@FXML
	private TableColumn<StoreBean, String> stoNameCol,inDateCol,ceoNameCol,corpNumCol,addressCol,phoneCol,hpCol,stoStateCol,outDateCol;
	@FXML
	private TableColumn<StoreBean, Integer> codeCol,barCol;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//getDB_info();
		try {
			ObservableList<String> barList = getBarName();
			barCom.setItems(barList);
			barCom.valueProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					sql = "SELECT main_code FROM branch_a WHERE bar_name='"+newValue+"'";
					try {
						pstmt = con.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if(rs.next()) {
							bar_id = rs.getInt("main_code");
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					
				}
				
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private ObservableList<String> getBarName() throws SQLException {
		ObservableList<String> comboList = FXCollections.observableArrayList();
		sql = "SELECT bar_name FROM branch_a WHERE state='Y'";
		rs = getRs(sql);
		while(rs.next()) {
			comboList.add(rs.getString("bar_name"));
		}
		return comboList;
	}
	
	private List<String> getDB_info() {
		try {
			BufferedReader br = new BufferedReader(new FileReader("C:/Garbage/Mirae_Net.dat"));
			String dbIp = br.readLine();
			list.add(dbIp);
			String dbName = br.readLine();			
			list.add(dbName);
			String dbPort = br.readLine();
			list.add(dbPort);
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	private Connection dbConn() {
		//String DBUrl="jdbc:sqlserver://"+list.get(0)+":"+list.get(2)+";databaseName="+list.get(1);
		String DBUrl="jdbc:sqlserver://210.99.49.107:9906;databaseName=dongnae_garbage";
		String user = "sa";
		String pwd = "kcmcard3003";
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			con = DriverManager.getConnection(DBUrl,user,pwd);
		} catch (Exception e) {
			e.printStackTrace();
			alert = new Alert(AlertType.ERROR);
			alert.setContentText("DB연결실패");
			alert.showAndWait();
		}
		return con;
	}
	
	private ObservableList<StoreBean> getStore(int bar_id) {
		ObservableList<StoreBean> storeList = FXCollections.observableArrayList();
		if(bar_id!=0) {
			sql = "SELECT * FROM store_info WHERE bar_id="+bar_id;
		} else {
			sql = "SELECT * FROM store_info";
		}
		try {
			rs = getRs(sql);
			while(rs.next()) {
				StoreBean store = new StoreBean();
				store.setKey_num(new SimpleIntegerProperty(rs.getInt("key_num")));
				store.setSto_name(new SimpleStringProperty(rs.getString("sto_name")));
				store.setIn_date(new SimpleStringProperty(rs.getString("in_date").substring(0, 10)));
				store.setCeo_name(new SimpleStringProperty(rs.getString("ceo_name")));
				store.setCorp_num(new SimpleStringProperty(rs.getString("corp_num")));
				store.setAddress(new SimpleStringProperty(rs.getString("address")));
				String main_phone = rs.getString("main_phone");
				if(!main_phone.equals("")) {
					if(main_phone.length()>=7) {
						store.setMain_phone(new SimpleStringProperty(main_phone.substring(0,3)+"-"+main_phone.substring(3,6)+"-"+main_phone.substring(6)));
					}
				}
				//store.setMain_phone(new SimpleStringProperty(main_phone));
				String ceo_hp = rs.getString("ceo_hp");
				if(!ceo_hp.equals("")) {
					if(ceo_hp.length()>=7) {
						store.setCeo_hp(new SimpleStringProperty(ceo_hp.substring(0,3)+"-"+ceo_hp.substring(3,7)+"-"+ceo_hp.substring(7)));
					}
				}
				//store.setCeo_hp(new SimpleStringProperty(ceo_hp));
				store.setBar_id(new SimpleIntegerProperty(rs.getInt("bar_id")));
				store.setStore_state(new SimpleStringProperty(rs.getString("store_state")));
				store.setOut_date(new SimpleStringProperty(rs.getString("out_date").substring(0, 10)));
				store.setTrade_state(new SimpleStringProperty(rs.getString("trade_state")));
				store.setC_upte(new SimpleStringProperty(rs.getString("c_upte")));
				store.setC_jongmok(new SimpleStringProperty(rs.getString("c_jongmok")));
				store.setE_mail(new SimpleStringProperty(rs.getString("e_mail")));
				storeList.add(store);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return storeList;	
	}
	
	@FXML
	private void tableSet() {
		storeList = getStore(bar_id);
		codeCol.setCellValueFactory(storeList->storeList.getValue().getKey_num().asObject());
		stoNameCol.setCellValueFactory(storeList->storeList.getValue().getSto_name());
		inDateCol.setCellValueFactory(storeList->storeList.getValue().getIn_date());
		ceoNameCol.setCellValueFactory(storeList->storeList.getValue().getCeo_name());
		corpNumCol.setCellValueFactory(storeList->storeList.getValue().getCorp_num());
		addressCol.setCellValueFactory(storeList->storeList.getValue().getAddress());
		phoneCol.setCellValueFactory(storeList->storeList.getValue().getMain_phone());
		hpCol.setCellValueFactory(storeList->storeList.getValue().getCeo_hp());
		barCol.setCellValueFactory(storeList->storeList.getValue().getBar_id().asObject());
		stoStateCol.setCellValueFactory(storeList->storeList.getValue().getStore_state());
		outDateCol.setCellValueFactory(storeList->storeList.getValue().getOut_date());
		storeTable.setItems(storeList);
	}
	
	private ResultSet getRs(String sql) throws SQLException {
		con = dbConn();
		pstmt  = con.prepareStatement(sql);
		rs = pstmt.executeQuery();
		return rs;
	}
	
	private void dbClose() {
		if(con!=null)try {con.close();} catch (Exception e) {}
		if(rs!=null)try {rs.close();} catch (Exception e) {}
		if(pstmt!=null)try {pstmt.close();} catch (Exception e) {}
		sql="";
	}
}
