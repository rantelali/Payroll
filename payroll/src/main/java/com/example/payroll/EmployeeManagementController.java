package com.example.payroll;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class EmployeeManagementController {

    @FXML private TextField nameField, departmentField, positionField, salaryField, hoursField;
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, Integer> idCol;
    @FXML private TableColumn<Employee, String> nameCol, deptCol, posCol;
    @FXML private TableColumn<Employee, Double> salaryCol, hoursCol;

    private ObservableList<Employee> employees = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        deptCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        posCol.setCellValueFactory(new PropertyValueFactory<>("position"));
        salaryCol.setCellValueFactory(new PropertyValueFactory<>("salary"));
        hoursCol.setCellValueFactory(new PropertyValueFactory<>("hours"));

        loadEmployees();

        employeeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                nameField.setText(newSel.getName());
                departmentField.setText(newSel.getDepartment());
                positionField.setText(newSel.getPosition());
                salaryField.setText(String.valueOf(newSel.getSalary()));
                hoursField.setText(String.valueOf(newSel.getHours()));
            }
        });
    }

    private void loadEmployees() {
        employees.clear();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM employees";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                employees.add(new Employee(
                        rs.getInt("employee_id"),
                        rs.getString("name"),
                        rs.getString("department"),
                        rs.getString("position"),
                        rs.getDouble("basic_salary"),
                        rs.getDouble("working_hours")
                ));
            }
            employeeTable.setItems(employees);
        } catch (SQLException e) {
            showAlert("DB Error", e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO employees (name, department, position, basic_salary, working_hours) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, departmentField.getText());
            pstmt.setString(3, positionField.getText());
            pstmt.setDouble(4, Double.parseDouble(salaryField.getText()));
            pstmt.setDouble(5, Double.parseDouble(hoursField.getText()));
            pstmt.executeUpdate();
            showAlert("Success", "Employee added.");
            loadEmployees();
            handleClear();
        } catch (SQLException e) {
            showAlert("Error Adding", e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select First", "No employee selected.");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE employees SET name=?, department=?, position=?, basic_salary=?, working_hours=? WHERE employee_id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, departmentField.getText());
            pstmt.setString(3, positionField.getText());
            pstmt.setDouble(4, Double.parseDouble(salaryField.getText()));
            pstmt.setDouble(5, Double.parseDouble(hoursField.getText()));
            pstmt.setInt(6, selected.getId());
            pstmt.executeUpdate();
            showAlert("Updated", "Employee record updated.");
            loadEmployees();
            handleClear();
        } catch (SQLException e) {
            showAlert("Error Updating", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select First", "No employee selected.");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM employees WHERE employee_id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, selected.getId());
            pstmt.executeUpdate();
            showAlert("Deleted", "Employee removed.");
            loadEmployees();
            handleClear();
        } catch (SQLException e) {
            showAlert("Error Deleting", e.getMessage());
        }
    }

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/payroll/AdminDashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) employeeTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load Admin Dashboard");
        }
    }


    @FXML
    private void handleClear() {
        nameField.clear();
        departmentField.clear();
        positionField.clear();
        salaryField.clear();
        hoursField.clear();
        employeeTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleRegisterEmployee() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/payroll/EmployeeRegisterDialog.fxml"));
            AnchorPane page = loader.load();


            Stage dialogStage = new Stage();
            dialogStage.setTitle("Register New Employee");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(employeeTable.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);


            EmployeeRegisterDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);


            dialogStage.showAndWait();

            if (controller.isRegistered()) {
                loadEmployees();
                showAlert("Success", "Employee registered successfully with login credentials.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load the registration form.");
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
