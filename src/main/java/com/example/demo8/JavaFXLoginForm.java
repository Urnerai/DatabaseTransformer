package com.example.demo8;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * Test Code
 */

public class JavaFXLoginForm extends Application {
    private String filePath;
    public ListView<String> tableListViewA = new ListView<>();
    public ListView<String> tableListViewB = new ListView<>();
    List<String> selectedTablesList = new ArrayList<>();
    private Alert alert;
    public ObservableList<String> tableDataA = FXCollections.observableArrayList();
    public ObservableList<String> tableDataB = FXCollections.observableArrayList("FDC_SKU_SUBGRP_DEF");

    public String ip;
    public String port;
    public String sid;
    public String dbURL;
    public String username;
    public String password;
    public PasswordField passwordTextField;
    public Database database1;
    public Database database2;



    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Oracle SQL to SQLite Migration");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(10);
        grid.setHgap(10);

        Scene scene = new Scene(grid, 500, 250);
        primaryStage.setScene(scene);


        //Login Screen UI Design

        Label usernameLabel = new Label("Kullanıcı Adı:");
        grid.add(usernameLabel, 0, 0);

        TextField usernameTextField = new TextField();
        usernameTextField.setPromptText(String.format("Kullanıcı Adı"));
        grid.add(usernameTextField, 1, 0);

        Label passwordLabel = new Label("Parola:");
        grid.add(passwordLabel, 0, 1);

        passwordTextField = new PasswordField();
        passwordTextField.setPromptText("Password");
        grid.add(passwordTextField, 1, 1);


        Label ipLabel = new Label("ip");
        grid.add(ipLabel, 2, 0);

        TextField ipTextField = new TextField();
        ipTextField.setPromptText(String.format("IP"));
        grid.add(ipTextField, 3, 0);

        Label portLabel = new Label("Port");
        grid.add(portLabel, 2, 1);

        TextField portTextField = new TextField();
        portTextField.setPromptText(String.format("Port"));
        grid.add(portTextField, 3, 1);

        Label sidLabel = new Label("SID");
        grid.add(sidLabel, 2, 2);

        TextField sidTextField = new TextField();
        sidTextField.setPromptText(String.format("sid"));
        grid.add(sidTextField, 3, 2);

        ToggleGroup toggleGroupD = new ToggleGroup();
        VBox vBoxD = new VBox(4);

        Label vboxLabelD = new Label("Database: ");
        grid.add(vboxLabelD, 0  , 3);

        RadioButton radioButtonOracle = new RadioButton("Oracle");
        radioButtonOracle.setToggleGroup(toggleGroupD);

        RadioButton radioButtonSQLite = new RadioButton("SQLite");
        radioButtonSQLite.setToggleGroup(toggleGroupD);

        RadioButton radioButtonMYSQL = new RadioButton("MYSQL");
        radioButtonMYSQL.setToggleGroup(toggleGroupD);

        RadioButton radioButtonPOSTSQL = new RadioButton("PostgreSQL");
        radioButtonPOSTSQL.setToggleGroup(toggleGroupD);

        vBoxD.getChildren().addAll(radioButtonOracle, radioButtonSQLite,radioButtonMYSQL,radioButtonPOSTSQL);
        grid.add(vBoxD, 1, 3);

        ToggleGroup toggleGroupD2 = new ToggleGroup();
        VBox vBoxD2 = new VBox(4);

        Label vboxLabelD2 = new Label("Transform to: ");
        grid.add(vboxLabelD2, 2  , 3);

        RadioButton radioButtonOracle2 = new RadioButton("Oracle");
        radioButtonOracle2.setToggleGroup(toggleGroupD2);

        RadioButton radioButtonSQLite2 = new RadioButton("SQLite");
        radioButtonSQLite2.setToggleGroup(toggleGroupD2);

        RadioButton radioButtonMYSQL2 = new RadioButton("MYSQL");
        radioButtonMYSQL2.setToggleGroup(toggleGroupD2);

        RadioButton radioButtonPOSTSQL2 = new RadioButton("PostgreSQL");
        radioButtonPOSTSQL2.setToggleGroup(toggleGroupD2);

        vBoxD2.getChildren().addAll(radioButtonOracle2, radioButtonSQLite2,radioButtonMYSQL2,radioButtonPOSTSQL2);
        grid.add(vBoxD2, 3, 3);

        Button loginButton = new Button("Giriş");
        grid.add(loginButton, 1, 2);




        //sidTextField.setDisable(true);


        //usernameTextField.setDisable(true);

        //passwordTextField.setDisable(true);


        //A listener for the ip field correction, when incorrect value is given, immediately deleted the given value

        ipTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,3}(\\.\\d{0,3}){0,3}")) {
                ipTextField.setText(oldValue); // Geçerli bir IP formatı değilse eski değeri geri yükle
            } else {
                String[] octets = newValue.split("\\.");
                boolean validIP = true;

                for (String octet : octets) {
                    if (!octet.isEmpty()) {
                        int value = Integer.parseInt(octet);
                        if (value < 0 || value > 255) {
                            validIP = false;
                            break;
                        }
                    }
                }

                if (validIP) {
                    ipTextField.setText(newValue); // Geçerli bir IP formatı ve değerlerse yeni değeri kabul et
                } else {
                    ipTextField.setText(oldValue); // Geçerli bir IP formatı ama değerler 0-255 arasında değilse eski değeri geri yükle
                }
            }
        });
        //A listener for the port field correction, when incorrect value is given, immediately deleted the given value
        portTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,5}")) {
                portTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });


        loginButton.setOnAction(event -> {
            ip = ipTextField.getText();

            port = portTextField.getText();

            sid = sidTextField.getText();
            dbURL = String.format("jdbc:oracle:thin:@%s:%s:%s", ip, port, sid);
            username = usernameTextField.getText().toLowerCase();
            password = passwordTextField.getText();
            //Login Screen Trigger, gets the connection with DB and prepares second screen(Choose tables, Browse filePath, choose processType Data or DDL etc.)

            try {
                try {
                    Class.forName("org.sqlite.JDBC");
                } catch (ClassNotFoundException e) {
                    System.err.println("JDBC sürücüsü bulunamadı: " + e.getMessage());
                }
                Connection connection = DriverManager.getConnection(dbURL, username, password); ////Connection with Oracle DB, "jdbc:sqlite:D:/sqld/sqlite/sqlite1"
                showAlert("Başarılı", "Oracle Veritabanına Bağlandı", Alert.AlertType.INFORMATION);


                if (radioButtonOracle.isSelected()){
                    database1=new Oracle();
                    System.out.println("555");
                }
                else if(radioButtonSQLite.isSelected()){
                    database1=new SQLite();
                }
                else if (radioButtonMYSQL.isSelected()){
                    database1=new MySQL();
                }
                else{
                    database1=new PostgreSQL();
                }

                if (radioButtonOracle2.isSelected()){
                    database2=new Oracle();

                }
                else if(radioButtonSQLite2.isSelected()){
                    database2=new SQLite();
                    System.out.println("222");
                }
                else if (radioButtonMYSQL2.isSelected()){
                    database2=new MySQL();
                }
                else{
                    database2=new PostgreSQL();
                }

                // Create a new GridPane for the new scene
                GridPane newGrid = new GridPane();
                newGrid.setPadding(new Insets(10, 10, 10, 10));
                newGrid.setVgap(10);
                newGrid.setHgap(10);

                // Add elements to the new grid for the new scene
                Label tableNameLabel = new Label("Table Name:");
                newGrid.add(tableNameLabel, 0, 0);

                TextField tableNameTextField = new TextField();
                tableNameTextField.setPromptText("Table Name");
                newGrid.add(tableNameTextField, 1, 0);
                tableNameTextField.setDisable(true);

                Label fileNameLabel = new Label("File Name: ");
                newGrid.add(fileNameLabel, 0, 1);

                TextField fileNameTextField = new TextField();
                fileNameTextField.setPromptText("File Name");
                newGrid.add(fileNameTextField, 1, 1);

                ToggleGroup toggleGroup = new ToggleGroup();
                VBox vBox = new VBox(2);

                Label vboxLabel = new Label("İşlem Tipi: ");
                newGrid.add(vboxLabel, 0, 2);

                RadioButton radioButton1 = new RadioButton("DDL");
                radioButton1.setToggleGroup(toggleGroup);


                RadioButton radioButton2 = new RadioButton("Data");
                radioButton2.setToggleGroup(toggleGroup);


                vBox.getChildren().addAll(radioButton1, radioButton2);
                newGrid.add(vBox, 1, 2);

                Label pathLabel = new Label("Dosya Yolu:");
                newGrid.add(pathLabel, 0, 3);

                TextField pathTextField = new TextField();
                pathTextField.setPromptText("Dosya Yolu");
                newGrid.add(pathTextField, 1, 3);
                pathTextField.setDisable(true);

                Button scene2Button = new Button("Ok");
                newGrid.add(scene2Button, 1, 4);

                Button scene2ButtonFilePath = new Button("Göz At");
                newGrid.add(scene2ButtonFilePath, 2, 3);

                Button scene2ButtonTableChoice = new Button("Tabloları Seçin");
                newGrid.add(scene2ButtonTableChoice, 2, 0);




                scene2ButtonFilePath.setOnAction(event1 -> {
                    //Browse explorer to save the file to chosen location

                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setTitle("Choose a Folder");
                    File selectedFolder = directoryChooser.showDialog(primaryStage);

                    if (selectedFolder != null) {
                        System.out.println("Selected folder: " + selectedFolder.getAbsolutePath());
                        filePath=selectedFolder.toString();
                        pathTextField.setDisable(false);
                        pathTextField.setText(filePath);
                        pathTextField.setDisable(true);

                    } else {
                        System.out.println("No folder selected.");
                    }


                });
                scene2ButtonTableChoice.setOnAction(event1-> {
                    //Gets tables from DB and choose them

                    try{
                        DatabaseMetaData databaseMetaData=connection.getMetaData();

                        ResultSet tables = databaseMetaData.getTables(null, null, null, new String[]{"TABLE"});

                        while (tables.next()) {
                            String tableName1 = tables.getString("TABLE_NAME");

                            tableDataA.addAll(tableName1);

                        }

                        tableListViewA.setItems(tableDataA);
                        tableListViewB.setItems(tableDataB);
                        System.out.println("--------------");
                        System.out.println(tableListViewA.getItems());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(tableListViewA.getItems());

                    tableListViewA.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


                    Dialog<ObservableList<String>> dialog = new Dialog<>();

                    ButtonType selectButtonType = new ButtonType("Seç", ButtonBar.ButtonData.OK_DONE);
                    if(!(radioButton1.isSelected()||radioButton2.isSelected())){
                        showAlert("Hata","Lütfen işlem tipi seçiniz.",Alert.AlertType.ERROR);
                    }
                    else{
                        dialog.setTitle("Tablo Seçimi");
                        dialog.setHeaderText("Lütfen bir veya daha fazla tablo seçiniz:");
                        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);


                    }

                    if(radioButton1.isSelected()){
                        //If DDL process Type is chosen do this.
                        dialog.getDialogPane().setContent(tableListViewA);
                        dialog.setResultConverter(dialogButton -> {

                            if (dialogButton == selectButtonType) {

                                return tableListViewA.getSelectionModel().getSelectedItems();
                            }

                            return null;
                        });
                    }
                    else if(radioButton2.isSelected()){
                        //If Data process Type is chosen do this.
                        dialog.getDialogPane().setContent(tableListViewB);
                        dialog.setResultConverter(dialogButton -> {

                            if (dialogButton == selectButtonType) {

                                return tableListViewB.getSelectionModel().getSelectedItems();
                            }

                            return null;
                        });
                    }
                    else{
                        dialog.getDialogPane().setContent(null);
                        return;
                    }
                    toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            selectedTablesList = FXCollections.observableArrayList();
                            tableNameTextField.setDisable(false);
                            tableNameTextField.setText("Table Name");
                            tableNameTextField.setDisable(true);
                            System.out.println(selectedTablesList);
                        }
                    });

                    Optional<ObservableList<String>> result = dialog.showAndWait();
                    result.ifPresent(selectedTables -> {

                        System.out.println("Seçilen tablo(lar): " + selectedTables);
                        selectedTablesList=selectedTables;

                        if (selectedTablesList != null) {
                            if (selectedTablesList.size()==1){
                                tableNameTextField.setDisable(false);
                                tableNameTextField.setText(selectedTablesList.toString());

                            }
                            else {
                                tableNameTextField.setDisable(false);
                                int selectedChoice=selectedTablesList.size();
                                tableNameTextField.setText(String.format("%o tane tablo seçildi.",selectedChoice));

                            }
                            tableNameTextField.setDisable(true);
                        }

                    });
                });
                Scene newScene = new Scene(newGrid, 500, 200);


                scene2Button.setOnAction(event1 -> {
                    //Scene 2 Screen Trigger, gets tables, process types, browses the explorer to save where location the file etc.
                    String tableName = tableNameTextField.getText();
                    String fileName = fileNameTextField.getText();

                    if (fileNameTextField.getText().isEmpty()) {
                        showAlert("Hata", "Dosya adı giriniz.", Alert.AlertType.ERROR);
                    }

                    else if(selectedTablesList.size()==0){
                        showAlert("Hata", "En az 1 tane tablo seçiniz.", Alert.AlertType.ERROR);
                    }
                    else if (filePath.isEmpty()){
                        showAlert("Hata", "Dosya yolu seçiniz.", Alert.AlertType.ERROR);
                    }
                    else{
                            if(radioButton1.isSelected()){
                                GridPane gridDDL=new GridPane();
                                Scene sceneDDL=new Scene(gridDDL,500,200);
                                Label textLabel=new Label("DDL Grid");
                                gridDDL.add(textLabel,0,0);
                                try {

                                    DatabaseMetaData databaseMetaData=connection.getMetaData();
                                    Statement statement = connection.createStatement();
                                    int sizeList=selectedTablesList.size();
                                    int tableCount=0;

                                    List<String> tables = new ArrayList<>();

                                    for(int i=0;i<sizeList;i++){
                                        tableName=selectedTablesList.get(i);

                                        ResultSet primaryKeys=databaseMetaData.getPrimaryKeys(null,null, tableName);

                                        String query = "SELECT * FROM " + tableName;

                                        ResultSet resultSet = statement.executeQuery(query);

                                        ResultSetMetaData colomns = resultSet.getMetaData();
                                        int numberofColumns = colomns.getColumnCount();

                                        List<String> colomnNames = new ArrayList<>();
                                        for (int j = 1; j <= numberofColumns; j++) {
                                            colomnNames.add(colomns.getColumnName(j));
                                        }


                                        ObservableList<String> colomnDataList = null;
                                        ObservableList<String> insertList = FXCollections.observableArrayList();
                                        ObservableList<String> colomnDataList1 = FXCollections.observableArrayList();
                                        int a=0;

                                        StringBuilder colomnDataListString = null;

                                        while (resultSet.next()) {
                                            colomnDataList= FXCollections.observableArrayList();
                                            int index2=0;

                                            for (String colomnNamesfor : colomnNames) {
                                                String colomnData = resultSet.getString(colomnNamesfor);
                                                colomnDataList.add(colomnData);
                                                System.out.println(colomnNamesfor+" = "+colomnData);
                                            }
                                            colomnDataListString=new StringBuilder();
                                            for(String item:colomnDataList){
                                                colomnDataListString.append("\"");
                                                colomnDataListString.append(item).append("\"");
                                                if (index2<colomnDataList.size()-1){
                                                    colomnDataListString.append(",");

                                                }

                                                index2++;
                                            }
                                            String concatenatedColomnDataListString = colomnDataListString.toString().trim();
                                            colomnDataList1.add("("+concatenatedColomnDataListString+")");
                                            System.out.println("for"+a+" "+colomnDataList);
                                            System.out.println("foryou"+a+" "+colomnDataList1);

                                            a=a+1;
                                        }
                                        int index=0;
                                        StringBuilder colomnNamesString=new StringBuilder();
                                        for (String item:colomnNames){
                                            colomnNamesString.append(item);
                                            if (index < colomnNames.size() - 1) {
                                                colomnNamesString.append(",");
                                            }
                                            index++;
                                        }
                                        String concatenatedColomnNames = colomnNamesString.toString().trim();

                                        StringBuilder colomnDataString=new StringBuilder();

                                        int index1=0;

                                        for (String item:colomnDataList1){
                                            colomnDataString.append(item);
                                            if (index1 < colomnDataList1.size()-1) {
                                                colomnDataString.append(",");
                                            }
                                            index1++;

                                        }

                                        String concatenatedColomnDatas = colomnDataString.toString().trim();

                                        String insertString = ((String.format("INSERT INTO %s (%s)\n VALUES%s",tableName,concatenatedColomnNames,concatenatedColomnDatas)));
                                        insertList.add(insertString);


                                        StringBuilder insertStringBuild=new StringBuilder();
                                        for(String item:insertList){
                                            insertStringBuild.append(item).append(";");
                                        }
                                        String concatenatedInsert = insertStringBuild.toString().trim();


                                        List<String> primaryKeysList = new ArrayList<>();
                                        List<String> colomnsList = new ArrayList<>();
                                        List<String> primaryKeysTestList = new ArrayList<>();
                                        List <String> columnName1=new ArrayList<>();



                                        int primaryKeyCount=0;
                                        int colomnsListCount=0;
                                        int primaryInfoCount=0;


                                        while (primaryKeys.next()) {
                                            String primaryKeyColumn = primaryKeys.getString("COLUMN_NAME");
                                            primaryKeysTestList.add(primaryKeyColumn);
                                        }


                                        for (int j = 1; j <= numberofColumns; j++) {
                                            String columnName = colomns.getColumnName(j);
                                            String columnType = colomns.getColumnTypeName(j);
                                            int columnTypeSize = colomns.getColumnDisplaySize(j);
                                            int isNullable = colomns.isNullable(j);
                                            String nullOrNotNull = (isNullable == 0) ? "NOT NULL" : "";

                                            if (primaryKeysTestList.contains(columnName)) {
                                                primaryKeysList.add(columnName + " ");
                                            }
                                            columnName1.add(columnName);
                                            /*
                                            Colomns information
                                             */
                                            colomnsList.add("\n"+"\t"+columnName + " " + columnType + "(" + columnTypeSize + ")" + " " + nullOrNotNull);
                                            colomnsListCount++;
                                        }

                                        //OracleToSQLiteDDLConverter converter=new OracleToSQLiteDDLConverter();
                                        //String convertedColomns1= converter.convertToSQLiteDDL(colomnsList.toString());
                                        System.out.println(colomnsList.toString());
                                        String convertedColomns=convertToxDDL(colomnsList.toString(),database1,database2);
                                        System.out.println("**********");
                                        System.out.println(convertedColomns);


                                        //String convertedPrimarys=converter.convertToSQLiteDDL(primaryKeysList.toString());
                                        String convertedPrimarys=convertToxDDL(primaryKeysList.toString(),database1,database2);
                                        /*
                                        convertedSQLite is for the SQLite query syntax
                                         */
                                        String convertedSQLite=String.format("DROP TABLE IF EXISTS %s;\n"+"CREATE TABLE %s"+"("+" "+convertedColomns+","+"\n"+"PRIMARY KEY("+convertedPrimarys+")"+")"+";\n"+concatenatedInsert,tableName,tableName,tableName);
                                        System.out.println(convertedSQLite);

                                        tables.add(convertedSQLite);
                                        tableCount=i;

                                    }

                                    System.out.println("3333333333");
                                    System.out.println(tableCount);


                                    StringBuilder tableString=new StringBuilder();


                                    for (String item:tables){
                                        tableString.append(item).append("\n");
                                    }
                                    String concatenatedString = tableString.toString().trim();

                                    writeToFile(fileName,concatenatedString,filePath);


                                } catch (SQLException e) {
                                    showAlert("Hata", e.getMessage(), Alert.AlertType.ERROR);

                                    e.printStackTrace();
                                }
                                //primaryStage.setScene(sceneDDL);
                            }
                            else{
                                //Data Grid UI Design

                                GridPane gridData=new GridPane();
                                Scene sceneData=new Scene(gridData,500,200);


                                ToggleGroup toggleGroup1 = new ToggleGroup();
                                VBox vBox1 = new VBox(3);

                                Label vboxLabel1 = new Label("İşlem Tipi: ");
                                gridData.add(vboxLabel1, 0, 1);

                                CheckBox checkBoxD = new CheckBox("Delete");
                                checkBoxD.setSelected(false);

                                CheckBox checkBoxI = new CheckBox("Insert");
                                checkBoxI.setSelected(false);

                                CheckBox checkBoxU = new CheckBox("Update");
                                checkBoxU.setSelected(false);

                                vBox1.getChildren().addAll(checkBoxD, checkBoxI, checkBoxU);

                                gridData.add(vBox1, 1, 1);
                                Button sceneDataButtonOk=new Button("Ok");
                                gridData.add(sceneDataButtonOk,2,1);

                                sceneDataButtonOk.setOnAction(event2 -> {
                                    //Data Screen Trigger, Send process types to oracle db to get the correct filtered value
                                    String tableName1=selectedTablesList.get(0);
                                    String a=null;
                                    String b=null;
                                    String c=null;
                                    String totalString=null;

                                    // This field checks the condition. Which buttons are chosen

                                    if(checkBoxD.isSelected()&&checkBoxI.isSelected()&&checkBoxU.isSelected()){
                                        a=queryCreator(connection,"D",tableName1);
                                        b=queryCreator(connection,"I",tableName1);
                                        c=queryCreator(connection,"U",tableName1);
                                        totalString=a+b+c;


                                    }
                                    else if(checkBoxI.isSelected()&&checkBoxU.isSelected()){
                                        a=queryCreator(connection,"I",tableName1);
                                        b=queryCreator(connection,"U",tableName1);
                                        totalString=a+b;

                                    }
                                    else if(checkBoxD.isSelected()&&checkBoxU.isSelected()){
                                        a=queryCreator(connection,"D",tableName1);
                                        b=queryCreator(connection,"U",tableName1);
                                        totalString=a+b;

                                    }
                                    else if(checkBoxD.isSelected()&&checkBoxI.isSelected()){
                                        a=queryCreator(connection,"D",tableName1);
                                        b=queryCreator(connection,"I",tableName1);
                                        totalString=a+b;

                                    }
                                    else if(checkBoxU.isSelected()){
                                        a=queryCreator(connection,"U",tableName1);
                                        totalString=a;

                                    }
                                    else if(checkBoxI.isSelected()){
                                        a=queryCreator(connection,"I",tableName1);
                                        totalString=a;

                                    }
                                    else if(checkBoxD.isSelected()){
                                        a=queryCreator(connection,"D",tableName1);
                                        totalString=a;

                                    }
                                    else{
                                        showAlert("Seç","Seçim yap",Alert.AlertType.ERROR);
                                    }
                                    writeToFile(fileName,totalString,filePath);



                                });
                                primaryStage.setScene(sceneData);
                                Button geriTusu=new Button("Geri");
                                gridData.add(geriTusu,3,1);

                                geriTusu.setOnAction(event2 -> {primaryStage.setScene(newScene);});

                            }

                    };
                });
                // Create a new scene with the new grid



                // Set the new scene to the primaryStage
                primaryStage.setScene(newScene);

            } catch (SQLException ex) {
                showAlert("Hata", ex.getMessage(), Alert.AlertType.ERROR);
                ex.printStackTrace();
            }
        });

        primaryStage.show();
    }



    public void showAlert(String title, String content, Alert.AlertType alertType) {
        alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * this method for write the content to chosen location
     * @param fileName what will txt name
     * @param content what will txt contains
     * @param filePath what will txt location
     */

    public void writeToFile(String fileName, String content,String filePath) {

        try {
            File file = new File(String.format("%s\\%s.txt",filePath,fileName));
            if(file.exists()){
                ButtonType buttonTypeOK = new ButtonType("Evet");
                ButtonType buttonTypeNO = new ButtonType("Hayır");

                alert.getButtonTypes().setAll(buttonTypeOK, buttonTypeNO);
                alert.setTitle("Üzerine yazmayı Onayla");
                alert.setContentText("Seçtiğiniz klasörde bu isimli bir dosya var. Üzerine yazmak istediğinize emin misiniz?");


                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent() && result.get() == buttonTypeOK) {
                        FileWriter fileWriter = new FileWriter(file);
                        fileWriter.write(content);
                        fileWriter.close();
                        System.out.println("Metin dosyası başarıyla oluşturuldu.");
                        showAlert("Başarılı",String.format("Veriler %s adlı dosyaya yazıldı.",fileName),Alert.AlertType.INFORMATION);
                }
                else {
                    Alert secondAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    secondAlert.getDialogPane().setGraphic(null);
                    secondAlert.setTitle("İptal Edildi");
                    secondAlert.setHeaderText("Dosya oluşturma işlemi iptal edildi.");
                    ButtonType buttonTypeOKOnly = new ButtonType("OK");
                    secondAlert.getButtonTypes().setAll(buttonTypeOKOnly);
                    secondAlert.showAndWait();
                }
            } else{
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(content);
                fileWriter.close();
                System.out.println("Metin dosyası başarıyla oluşturuldu.");
                showAlert("Başarılı",String.format("Veriler %s adlı dosyaya yazıldı.",fileName),Alert.AlertType.INFORMATION);
            }

        } catch (IOException e) {
            System.out.println("Hata oluştu: " + e.getMessage());
        }
    }


    /**
     * thıs ıs a creator functıon
     * @param connection *gets connection with jdbc in document
     * @param processType * when the choice "Data" gets the process type for the data query
     * @param tableName * chosen tables
     * @return create a String with all the list data then return this string "Conconated String"
     */
    public String queryCreator(Connection connection, String processType, String tableName){
        String concatenatedData = null;
        try {
            String query=String.format("SELECT * FROM %s WHERE(main_grp, sub_grp) IN (SELECT pk1, pk2\n" +
                    "    FROM transfer_table  \n" +
                    "    WHERE trs_flag = 'NO' \n" +
                    "    AND table_name = 'FDC_SKU_SUBGRP_DEF' \n" +
                    "    AND process_type = '%s'\n" +
                    ")",tableName,processType);
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String, List<String>> res = new HashMap<>();
            String pk1 = null;
            String pk2 = null;
            String main_GRP_ex=null;
            String sub_GRP_ex=null;

            ObservableList<String> dataList = FXCollections.observableArrayList();

                if (processType=="D"){
                    while (resultSet.next()){

                        pk1=resultSet.getString("MAIN_GRP");
                        pk2=resultSet.getString("SUB_GRP");

                        System.out.println(tableName+" "+pk1+" "+pk2+" ");
                        String data=String.format("DELETE FROM %s WHERE MAIN_GRP='%s' AND SUB_GRP='%s'",tableName,pk1,pk2);
                        dataList.add(data);
                    }
                } else if (processType=="I") {
                    while (resultSet.next()){
                        pk1=resultSet.getString("MAIN_GRP");
                        pk2=resultSet.getString("SUB_GRP");
                        main_GRP_ex=resultSet.getString("MAIN_GRP_EXPLANATION");
                        sub_GRP_ex=resultSet.getString("SUB_GRP_EXPLANATION");

                        System.out.println(tableName+" "+pk1+" "+pk2+" ");
                        String data=String.format("INSERT INTO %s (MAIN_GRP, SUB_GRP, SUB_GRP_EXPLANATION, MAIN_GRP_EXPLANATION)\n" +
                                "SELECT '%s', '%s', '%s', '%s'\n" +
                                "WHERE NOT EXISTS (\n" +
                                "    SELECT 1\n" +
                                "    FROM %s\n" +
                                "    WHERE MAIN_GRP = '%s' AND SUB_GRP = '%s'\n" +
                                ")",tableName,pk1,pk2,sub_GRP_ex,main_GRP_ex,tableName,pk1,pk2);
                        dataList.add(data);
                    }
                }
                else if(processType=="U"){
                    while (resultSet.next()){
                        pk1=resultSet.getString("MAIN_GRP");
                        pk2=resultSet.getString("SUB_GRP");
                        main_GRP_ex=resultSet.getString("MAIN_GRP_EXPLANATION");
                        sub_GRP_ex=resultSet.getString("SUB_GRP_EXPLANATION");

                        System.out.println(tableName+" "+pk1+" "+pk2+" ");
                        String data=String.format("UPDATE %s\n" +
                                "SET SUB_GRP_EXPLANATION = '%s', MAIN_GRP_EXPLANATION = '%s'\n" +
                                "WHERE MAIN_GRP = '%s' AND SUB_GRP = '%s';\n",tableName,sub_GRP_ex,main_GRP_ex,pk1,pk2);
                        dataList.add(data);
                    }
                }

            StringBuilder stringBuilder=new StringBuilder();
            for (String item:dataList){
                int index=0;
                stringBuilder.append(item);
                if (index<dataList.size()-1){
                    stringBuilder.append(";\n");
                }
            }
            concatenatedData=stringBuilder.toString().trim();

        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return concatenatedData;

    }



    public String convertToxDDL(String xDDL, Database database1, Database database2) {
        xDDL = xDDL.replace("[", "");
        xDDL = xDDL.replace("]", "");
        xDDL = xDDL.replaceAll(database1.getInteger(), database2.getInteger());
        xDDL = xDDL.replaceAll(database1.getDate(), database2.getDate());
        xDDL = xDDL.replaceAll(database1.getVarchar(), database2.getVarchar());
        xDDL = xDDL.replace(database1.getNotNull(), database2.getNotNull());
        return xDDL;
    }

    public static class Database {
        protected String integer;
        protected String date;
        protected String varchar;
        protected String notNull;
        protected String timeStamp;
        protected String bool;
        protected String text;
        protected String decimal;
        protected String character;
        protected String blob;




        public String getInteger() {
            return integer;
        }

        public String getDate() {
            return date;
        }

        public String getVarchar() {
            return varchar;
        }

        public String getNotNull() {
            return notNull;
        }
        public String getTimeStamp() {
            return timeStamp;
        }

        public String getBool() {
            return bool;
        }

        public String getText() {
            return text;
        }

        public String getDecimal() {
            return decimal;
        }

        public String getCharacter() {
            return character;
        }

        public String getBlob() {
            return blob;
        }
    }

    public static class Oracle extends Database {
        public Oracle() {
            integer = "NUMBER";
            date = "DATE";
            varchar = "VARCHAR2";
            notNull = "NOT NULL ENABLE";
            timeStamp="TIMESTAMP";
            bool="NUMBER(1)";
            text="CLOB";
            decimal="NUMBER";
            character="CHAR";
            blob="BLOB";

        }
    }

    public static class SQLite extends Database {
        public SQLite() {
            integer = "INTEGER";
            date = "TEXT";
            varchar = "TEXT";
            notNull = "NOT NULL";
            timeStamp = "TEXT";
            bool = "INTEGER";
            text = "TEXT";
            decimal = "NUMERIC";
            character = "TEXT";
            blob = "BLOB";
        }
    }
    public static class MySQL extends Database {

        public MySQL() {
            integer = "INT";
            date = "DATE";
            varchar = "VARCHAR";
            notNull = "NOT NULL";
            timeStamp = "TIMESTAMP";
            bool = "TINYINT(1)";
            text = "TEXT";
            decimal = "DECIMAL";
            character = "CHAR";
            blob = "BLOB";
        }
    }
    public static class PostgreSQL extends Database {

        public PostgreSQL() {
            integer = "INT";
            date = "DATE";
            varchar = "VARCHAR";
            notNull = "NOT NULL";
            timeStamp = "TIMESTAMP";
            bool = "BOOLEAN";
            text = "TEXT";
            decimal = "DECIMAL";
            character = "CHAR";
            blob = "BYTEA";

        }
    }

}
