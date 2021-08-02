package lk.ijse.dep7.SMSLite.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import lk.ijse.dep7.SMSLite.service.StudentTM;

import java.sql.*;
import java.util.List;
import java.util.Scanner;

public class StudentFormController {
    public TextField txtID;
    public TextField txtName;
    public TextField txtPhone;
    public ListView<String> lstPhone;
    public Button btnClear;
    public Button btnRemove;
    public Button btnSave;
    public TableView<StudentTM> tblStudent;
    public AnchorPane pneStudent;
    private Connection connection;

    public void initialize(){

        btnSave.setDisable(true);

        tblStudent.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblStudent.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblStudent.getColumns().get(2).setCellValueFactory(new PropertyValueFactory("abc"){
            @Override
            public ObservableValue call(TableColumn.CellDataFeatures param) {
                return new ObservableValueBase() {
                    @Override
                    public Object getValue() {
                        Button delete = new Button("Delete");
                        delete.setOnAction(event -> {
                        StudentTM student = (StudentTM) param.getValue();
                        String studentId = student.getId();
                            try {
                                String sql;
                                sql = "DELETE FROM phone_number WHERE id = '"+ studentId +"';";
                                Statement stm = connection.createStatement();
                                int deletedRow = stm.executeUpdate(sql);

                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            try {
                                String sql;
                                sql = "DELETE FROM student_details WHERE id = '"+ studentId +"';";
                                Statement stm = connection.createStatement();
                                int deletedRow = stm.executeUpdate(sql);

                                if (deletedRow == 1){
                                    new Alert(Alert.AlertType.CONFIRMATION,"One Recorded Has Been Deleted",ButtonType.CLOSE).show();
                                }else {
                                    new Alert(Alert.AlertType.ERROR,"Failed to delete",ButtonType.CLOSE).show();
                                }

                                tblStudent.refresh();


                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            tblStudent.getItems().remove(param.getValue());
                        });
                        return delete;
                    }
                };
            }
        });





        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {

            btnSave.setDisable(!(txtID.getText().matches("S\\d{3}") &&
                    txtName.getText().matches("[a-zA-Z]{3,}") &&
                    !(lstPhone.getItems().isEmpty())));
        };

        txtID.textProperty().addListener(changeListener);
        txtName.textProperty().addListener(changeListener);
        txtPhone.textProperty().addListener(changeListener);

        tblStudent.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedStudent) -> {
            if (selectedStudent != null) {
                String id = selectedStudent.getId();
                String name = selectedStudent.getName();

                try {
                    String sql;
                    sql = "SELECT * FROM phone_number WHERE id = '"+ id +"' ";
                    Statement stm = connection.createStatement();
                    lstPhone.getItems().clear();
                    ResultSet rst = stm.executeQuery(sql);
                    while (rst.next()){
                        lstPhone.getItems().add(rst.getString("numbers"));
                    }
                    txtID.setText(id);
                    txtName.setText(name);
                    lstPhone.refresh();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                txtID.setDisable(true);
                btnSave.setText("Update");
                btnSave.setDisable(false);

            } else {
                btnSave.setText("Save");
            }

        });

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/student", "root", "th15@rud@51th");

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR,"Failed to connect to server",ButtonType.CLOSE).showAndWait();
            e.printStackTrace();
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if(!connection.isClosed()){
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));


        try {
            String sql;
            sql = "SELECT * FROM student_details";
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery(sql);
            while (rst.next()){
                String id = rst.getString("id");
                String name = rst.getString("name");
                tblStudent.getItems().add(new StudentTM(id,name));
            }

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR,"Failed to lord details to table").show();
            e.printStackTrace();
        }

        lstPhone.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> txtPhone.setText(newValue));

    }


    public void btnClear_OnAction(ActionEvent actionEvent) {
        lstPhone.getItems().clear();
    }

    public void btnRemove_OnAction(ActionEvent actionEvent) {

        int selectedIndex = lstPhone.getSelectionModel().getSelectedIndex();
        lstPhone.getItems().remove(selectedIndex);
        lstPhone.refresh();

    }

    public void btnSave_OnAction(ActionEvent actionEvent) {

        if (btnSave.getText().equals("Save")){
            String id = txtID.getText();
            String name = txtName.getText();

            try {
                Statement stm = connection.createStatement();
                String sql;
                sql = "SELECT id FROM student_details WHERE id = '"+ id +"'";
                ResultSet rs = stm.executeQuery(sql);
                if (rs.next()){
                    new Alert(Alert.AlertType.ERROR,"Customer Id already exits").show();
                    txtID.requestFocus();
                    return;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }


            try {
                String sql;
                sql ="INSERT INTO student_details (id,name) VALUE ('%s','%s');";
                sql = String.format(sql, id, name);
                Statement stm= connection.createStatement();
                int affectedRow = stm.executeUpdate(sql);
                if (affectedRow ==1){
                    new Alert(Alert.AlertType.CONFIRMATION,"One Record Has Been Added",ButtonType.OK).show();
                }else {
                    new Alert(Alert.AlertType.ERROR,"Cannot add the record",ButtonType.CLOSE).show();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                List<String> listView = lstPhone.getItems();

                for (String numbers:listView) {
                    String sql;
                    sql ="INSERT INTO phone_number (id,numbers) VALUE ('%s','%s');";
                    Statement stm= connection.createStatement();
                    sql = String.format(sql, id, numbers);
                    stm.executeUpdate(sql);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            tblStudent.getItems().add(new StudentTM(id,name));


        }else {

         StudentTM selectedItem = tblStudent.getSelectionModel().getSelectedItem();
         selectedItem.setId(txtID.getText());
         selectedItem.setName(txtName.getText());
         tblStudent.refresh();

         String id = txtID.getText();
         String name = txtName.getText();
         String number = txtPhone.getText();

            try {
                String sql;
                sql = "UPDATE student_details SET name = '"+ name + "' WHERE id = '"+ id +"';";
                Statement stm = connection.createStatement();
                int updatedValue = stm.executeUpdate(sql);

                if (updatedValue ==1){
                    new Alert(Alert.AlertType.CONFIRMATION,"One Record Has Been Updated",ButtonType.OK).show();
                }else {
                    new Alert(Alert.AlertType.ERROR,"Failed to update the record",ButtonType.CLOSE).show();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        txtID.clear();
        txtName.clear();
        txtPhone.clear();
        lstPhone.getItems().clear();
    }

    public void pneStudent_Onkey(KeyEvent keyEvent) {
       if (keyEvent.getCode()== KeyCode.ENTER && txtPhone.getText().matches("\\d{10}")){
           lstPhone.getItems().add(txtPhone.getText());
           txtPhone.clear();

       }
    }
}
