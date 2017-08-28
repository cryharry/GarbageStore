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

public class GarbageController implements Initializable{
	List<String> list = new ArrayList<String>();
	Connection con;
	PreparedStatement pstmt;
	ResultSet rs;
	String sql, bar_name;
	int bar_id = 0;
	
	@FXML
	Alert alert;
	@FXML
	private ComboBox<String> barCom;
	@FXML
	private Button searchBtn, excelBtn;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		getDB_info();
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
	private void getStore(int bar_id) {
		con = dbConn();
		if(bar_id!=0) {
			sql = "SELECT * FROM store_info WHERE bar_id="+bar_id;
			try {
				pstmt = con.prepareStatement(sql);
				rs = pstmt.executeQuery();
				List<StoreBean> storeList = new ArrayList<StoreBean>();
				while(rs.next()) {
					StoreBean store = new StoreBean();
					store.setKey_num(rs.getInt("key_num"));
					store.setSto_name(rs.getString("sto_name"));
					store.setIn_date(rs.getString("in_date").substring(0, 10));
					store.setCeo_name(rs.getString("ceo_name"));
					store.setCorp_num(rs.getString("corp_num"));
					store.setAddress(rs.getString("address"));
					String main_phone = rs.getString("main_phone");
					store.setMain_phone(main_phone.substring(0,2)+"-"+main_phone.substring(3,5)+"-"+main_phone.substring(6));
					String ceo_hp = rs.getString("ceo_hp");
					store.setMain_phone(ceo_hp.substring(0,2)+"-"+ceo_hp.substring(3,6)+"-"+ceo_hp.substring(7));
					store.setBar_id(rs.getInt("bar_id"));
					storeList.add(store);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private ResultSet getRs(String sql) throws SQLException {
		con = dbConn();
		pstmt  = con.prepareStatement(sql);
		rs = pstmt.executeQuery();
		return rs;
	}
}
