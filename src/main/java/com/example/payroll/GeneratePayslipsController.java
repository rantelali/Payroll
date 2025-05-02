package com.example.payroll;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Objects;

import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileWriter;


public class GeneratePayslipsController {

    @FXML private ComboBox<String> employeeComboBox;
    @FXML private TextArea payslipArea;

    private ObservableList<String> employeeNames = FXCollections.observableArrayList();
    private final String payslipTemplate = """
            Employee Payslip
            --------------------------
            Name: %s
            Department: %s
            Position: %s
            Basic Salary: M%.2f
            Working Hours: %.2f
            --------------------------
            Gross Salary: M%.2f
            Total Deductions: M%.2f
            Net Salary: M%.2f
            Pay Date: %s
            """;

    @FXML
    public void initialize() {
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT name FROM employees");
            while (rs.next()) {
                employeeNames.add(rs.getString("name"));
            }
            employeeComboBox.setItems(employeeNames);
        } catch (SQLException e) {
            payslipArea.setText("Error loading employees: " + e.getMessage());
        }
    }

    @FXML
    private void handleEmployeeSelect() {
        String name = employeeComboBox.getValue();
        if (name == null) return;

        try (Connection conn = DBConnection.getConnection()) {

            String empSql = "SELECT * FROM employees WHERE name=?";
            PreparedStatement empStmt = conn.prepareStatement(empSql);
            empStmt.setString(1, name);
            ResultSet empRs = empStmt.executeQuery();

            if (empRs.next()) {
                int empId = empRs.getInt("employee_id");
                String department = empRs.getString("department");
                String position = empRs.getString("position");
                double basicSalary = empRs.getDouble("basic_salary");
                double hours = empRs.getDouble("working_hours");


                String paySql = "SELECT * FROM payroll WHERE employee_id=? ORDER BY pay_date DESC LIMIT 1";
                PreparedStatement payStmt = conn.prepareStatement(paySql);
                payStmt.setInt(1, empId);
                ResultSet payRs = payStmt.executeQuery();

                if (payRs.next()) {
                    String formatted = String.format(
                            payslipTemplate,
                            name, department, position, basicSalary, hours,
                            payRs.getDouble("gross_salary"),
                            payRs.getDouble("total_deductions"),
                            payRs.getDouble("net_salary"),
                            payRs.getDate("pay_date").toString()
                    );
                    payslipArea.setText(formatted);
                } else {
                    payslipArea.setText("No payroll record found for this employee.");
                }
            }
        } catch (SQLException e) {
            payslipArea.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToDashboard(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/payroll/AdminDashboard.fxml")));
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleExport() {
        String payslip = payslipArea.getText();
        if (payslip.isEmpty()) {
            showAlert("Export Error", "No payslip to export. Please select an employee first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Payslip");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("payslip.txt");

        Stage stage = (Stage) payslipArea.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(payslip);
                showAlert("Success", "Payslip exported successfully!");
            } catch (IOException e) {
                showAlert("Export Error", "Failed to write file: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }




    @FXML
    private void handleBack() {

        payslipArea.clear();
        employeeComboBox.getSelectionModel().clearSelection();
    }
}
