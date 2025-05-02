package com.example.payroll;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Objects;

public class PayrollReportsController {

    @FXML private ComboBox<String> departmentComboBox;
    @FXML private BarChart<String, Number> barChart;
    @FXML private BarChart<String, Number> departmentChart;

    @FXML
    public void initialize() {
        loadDepartments();
        loadEmployeeChart(null);
        loadDepartmentSummaryChart();
    }

    private void loadDepartments() {
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT DISTINCT department FROM employees WHERE department IS NOT NULL");
            var departments = FXCollections.<String>observableArrayList();
            departments.add("All");
            while (rs.next()) {
                departments.add(rs.getString("department"));
            }
            departmentComboBox.setItems(departments);
            departmentComboBox.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void filterByDepartment() {
        String selected = departmentComboBox.getValue();
        loadEmployeeChart("All".equals(selected) ? null : selected);
    }

    private void loadEmployeeChart(String departmentFilter) {
        barChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Net Salary");

        String sql = """
            SELECT e.name, p.net_salary
            FROM payroll p
            JOIN employees e ON e.employee_id = p.employee_id
            WHERE p.pay_date = (SELECT MAX(pay_date) FROM payroll p2 WHERE p2.employee_id = p.employee_id)
        """;

        if (departmentFilter != null) {
            sql += " AND e.department = ?";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (departmentFilter != null) {
                stmt.setString(1, departmentFilter);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("name"), rs.getDouble("net_salary")));
            }

            barChart.getData().add(series);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDepartmentSummaryChart() {
        departmentChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total Net Salary by Dept");

        String sql = """
            SELECT e.department, SUM(p.net_salary) AS total
            FROM payroll p
            JOIN employees e ON e.employee_id = p.employee_id
            WHERE p.pay_date = (
                SELECT MAX(pay_date)
                FROM payroll p2
                WHERE p2.employee_id = p.employee_id
            )
            GROUP BY e.department
            ORDER BY total DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("department"), rs.getDouble("total")));
            }

            departmentChart.getData().add(series);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        barChart.getData().clear();
        departmentChart.getData().clear();
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

}
