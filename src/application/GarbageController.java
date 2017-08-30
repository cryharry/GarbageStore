package application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class GarbageController implements Initializable{
	List<String> list = new ArrayList<String>();
	Connection con;
	PreparedStatement pstmt;
	ResultSet rs;
	String sql, bar_name, excelPath;
	int bar_id = 0;
	ObservableList<StoreBean> storeList;
	StoreBean store;
	FileChooser fc;
	File excelName;
	
	@FXML
	private Stage primaryStage;
	@FXML
	private Alert alert;
	@FXML
	private ComboBox<String> barCom;
	@FXML
	private Button searchBtn, excelBtn;
	@FXML
	private TableView<StoreBean> storeTable;
	@FXML
	private TableColumn<StoreBean, String> stoNameCol,inDateCol,ceoNameCol,corpNumCol,addressCol,phoneCol,hpCol,stoStateCol,outDateCol,barCol;
	@FXML
	private TableColumn<StoreBean, Integer> codeCol;
	@FXML
	private RadioButton yesState, noState;

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
	
	private Connection dbConn() {
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
		storeList = FXCollections.observableArrayList();
		sql = "SELECT store.key_num,sto_name,store.in_date,store.ceo_name,store.corp_num,store.address" + 
				" ,store.main_phone,ceo_hp,bar_id,store_state,out_date,trade_state,store.c_upte,store.c_jongmok,e_mail" + 
				" ,branch.bar_name FROM store_info as store INNER JOIN branch_a branch ON store.bar_id=branch.main_code WHERE trade_state = 'Y'";
		if(bar_id!=0) {
			sql +=  "and store.bar_id="+bar_id;
		}
		sql += "ORDER BY sto_name";
		try {
			rs = getRs(sql);
			while(rs.next()) {
				store = new StoreBean();
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
					} else if(main_phone.length()>3 && main_phone.length()<7) {
						store.setMain_phone(new SimpleStringProperty(main_phone.substring(0,3)+"-"+main_phone.substring(3)));
					} else {
						store.setMain_phone(new SimpleStringProperty(main_phone));
					}
				}
				String ceo_hp = rs.getString("ceo_hp");
				if(!ceo_hp.equals("")) {
					if(ceo_hp.length()>=7) {
						store.setCeo_hp(new SimpleStringProperty(ceo_hp.substring(0,3)+"-"+ceo_hp.substring(3,7)+"-"+ceo_hp.substring(7)));
					} else if(ceo_hp.length()>3 && ceo_hp.length()<7) {
						store.setCeo_hp(new SimpleStringProperty(ceo_hp.substring(0,3)+"-"+ceo_hp.substring(3)));
					} else {
						store.setCeo_hp(new SimpleStringProperty(ceo_hp));
					}
				}
				store.setBar_name(new SimpleStringProperty(rs.getString("bar_name")));
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
		barCol.setCellValueFactory(storeList->storeList.getValue().getBar_name());
		stoStateCol.setCellValueFactory(storeList->storeList.getValue().getStore_state());
		outDateCol.setCellValueFactory(storeList->storeList.getValue().getOut_date());
		storeTable.setItems(storeList);
	}
	
	@FXML
	private void excelSave() throws EncryptedDocumentException, InvalidFormatException, IOException {
		storeList = getStore(bar_id);
		fc = new FileChooser();
		fc.setTitle("다른 이름으로 저장 - xls");
		FileChooser.ExtensionFilter xlsFilter = new FileChooser.ExtensionFilter("xls file(*.xls)", "*.xls");
		fc.getExtensionFilters().add(xlsFilter);
		excelName =  fc.showSaveDialog(primaryStage);
		excelPath = excelName.getPath().replace("\\", "/");
		
		Workbook xls = new HSSFWorkbook();
		FileOutputStream fos = new FileOutputStream(excelPath);
		Sheet sheet = xls.createSheet("판매소현황");
		Row headRow = sheet.createRow(0);
		for(int z=0; z<=11; z++) {
			Cell headCell = headRow.createCell(z);
			switch (z) {
			case 0:
				headCell.setCellValue("코드번호");
				break;
			case 1:
				headCell.setCellValue("판매소명");
				break;
			case 2:
				headCell.setCellValue("계약일자");
				break;
			case 3:
				headCell.setCellValue("대표자명");
				break;
			case 4:
				headCell.setCellValue("사업자번호");
				break;
			case 5:
				headCell.setCellValue("주   소");
				break;
			case 6:
				headCell.setCellValue("전화번호");
				break;
			case 7:
				headCell.setCellValue("폰번호");
				break;
			case 8:
				headCell.setCellValue("관할금고명");
				break;
			case 9:
				headCell.setCellValue("판매소구분");
			case 10:
				headCell.setCellValue("해지일자");
				break;
			default:
				break;
			}
		}
		int rowNum = 1;
		for(int i=0; i<storeList.size();i++) {
			Row row = sheet.createRow(rowNum++);
			int colNum = 0;
			for(int j=0; j<=11; j++) {
				Cell cell = row.createCell(colNum++);
				switch (j) {
				case 0:
					cell.setCellValue(storeList.get(i).getKey_num().getValue().intValue());
					break;
				case 1:
					cell.setCellValue(storeList.get(i).getSto_name().getValue());
					break;
				case 2:
					cell.setCellValue(storeList.get(i).getIn_date().getValue());
					break;
				case 3:
					cell.setCellValue(storeList.get(i).getCeo_name().getValue());
					break;
				case 4:
					cell.setCellValue(storeList.get(i).getCorp_num().getValue());
					break;
				case 5:
					cell.setCellValue(storeList.get(i).getAddress().getValue());
					break;
				case 6:
					if(storeList.get(i).getMain_phone()!=null)
						cell.setCellValue(storeList.get(i).getMain_phone().getValue());
					break;
				case 7:
					if(storeList.get(i).getCeo_hp()!=null)
					cell.setCellValue(storeList.get(i).getCeo_hp().getValue());
					break;
				case 8:
					cell.setCellValue(storeList.get(i).getBar_name().getValue());
					break;
				case 9:
					cell.setCellValue(storeList.get(i).getStore_state().getValue());
					break;
				case 10:
					cell.setCellValue(storeList.get(i).getOut_date().getValue());
					break;
				default:
					break;
				}
			}
		}
		xls.write(fos);
		fos.close();
		alert = new Alert(AlertType.INFORMATION);
		alert.setContentText("저장완료");
		alert.showAndWait();
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
