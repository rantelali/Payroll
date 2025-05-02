package com.example.payroll;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SalaryCalculationController {

    @FXML private ComboBox<String> departmentComboBox;
    @FXML private ComboBox<String> employeeComboBox;

    @FXML private Label salaryLabel, hoursLabel, resultLabel;

    @FXML private TextField overtimeField, taxField, insuranceField, otherDeductionField;

    private Map<String, Integer> employeeMap = new HashMap<>();
    private Map<String, Double> salaryMap = new HashMap<>();
    private Map<String, Double> hoursMap = new HashMap<>();

    @FXML
    public void initialize() {
        loadDepartments();
    }

    private void loadDepartments() {
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT DISTINCT department FROM employees WHERE department IS NOT NULL");
            ObservableList<String> departments = FXCollections.observableArrayList();
            while (rs.next()) {
                departments.add(rs.getString("department"));
            }
            departmentComboBox.setItems(departments);
        } catch (SQLException e) {
            resultLabel.setText("Error loading departments: " + e.getMessage());
        }
    }

    @FXML
    private void handleDepartmentFilter() {
        String selectedDept = departmentComboBox.getValue();
        if (selectedDept == null) return;

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT employee_id, name, basic_salary, working_hours FROM employees WHERE department=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, selectedDept);
            ResultSet rs = pstmt.executeQuery();

            ObservableList<String> names = FXCollections.observableArrayList();
            employeeMap.clear();
            salaryMap.clear();
            hoursMap.clear();

            while (rs.next()) {
                String name = rs.getString("name");
                int id = rs.getInt("employee_id");
                double salary = rs.getDouble("basic_salary");
                double hours = rs.getDouble("working_hours");

                employeeMap.put(name, id);
                salaryMap.put(name, salary);
                hoursMap.put(name, hours);
                names.add(name);
            }

            employeeComboBox.setItems(names);
        } catch (SQLException e) {
            resultLabel.setText("Error loading employees: " + e.getMessage());
        }
    }


    @FXML
    private void handleEmployeeSelection() {
        String name = employeeComboBox.getValue();
        if (name == null) return;

        salaryLabel.setText("M" + salaryMap.get(name));
        hoursLabel.setText(hoursMap.get(name).toString());
    }

    @FXML
    private void handleCalculate() {
        String name = employeeComboBox.getValue();
        if (name == null || !employeeMap.containsKey(name)) {
            resultLabel.setText("Please select an employee.");
            return;
        }

        try {
            int empId = employeeMap.get(name);
            double basicSalary = salaryMap.get(name);
            double overtime = Double.parseDouble(overtimeField.getText());
            double tax = basicSalary * Double.parseDouble(taxField.getText()) / 100.0;
            double insurance = Double.parseDouble(insuranceField.getText());
            double other = Double.parseDouble(otherDeductionField.getText());

            double gross = basicSalary + overtime;
            double totalDeductions = tax + insurance + other;
            double net = gross - totalDeductions;

            try (Connection conn = DBConnection.getConnection()) {

                String dedSql = "INSERT INTO deductions (employee_id, tax, insurance, other) VALUES (?, ?, ?, ?)";
                PreparedStatement dedStmt = conn.prepareStatement(dedSql);
                dedStmt.setInt(1, empId);
                dedStmt.setDouble(2, tax);
                dedStmt.setDouble(3, insurance);
                dedStmt.setDouble(4, other);
                dedStmt.executeUpdate();

                // Save payroll
                String paySql = "INSERT INTO payroll (employee_id, gross_salary, total_deductions, net_salary, pay_date) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement payStmt = conn.prepareStatement(paySql);
                payStmt.setInt(1, empId);
                payStmt.setDouble(2, gross);
                payStmt.setDouble(3, totalDeductions);
                payStmt.setDouble(4, net);
                payStmt.setDate(5, Date.valueOf(LocalDate.now()));
                payStmt.executeUpdate();

                resultLabel.setText("Net Salary for " + name + ": M" + net);

            } catch (SQLException e) {
                resultLabel.setText("DB error: " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            resultLabel.setText("Enter valid numbers for salary fields.");
        }
    }

    @FXML
    private void handleClear() {
        overtimeField.clear();
        taxField.clear();
        insuranceField.clear();
        otherDeductionField.clear();
        resultLabel.setText("");
        salaryLabel.setText("-");
        hoursLabel.setText("-");
        employeeComboBox.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleBackToDashboard(javafx.event.ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/payroll/AdminDashboard.fxml")));
            Stage stage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
