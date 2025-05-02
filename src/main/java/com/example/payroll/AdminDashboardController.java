package com.example.payroll;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import java.io.IOException;
import java.util.Objects;

public class AdminDashboardController {

    @FXML
    private void handleManageEmployees(ActionEvent event) {
        loadScene("/com/example/payroll/EmployeeManagement.fxml", event, "Manage Employees");
    }

    @FXML
    private void handleSalaryCalculation(ActionEvent event) {
        loadScene("/com/example/payroll/SalaryCalculation.fxml", event, "Process Salary");
    }

    @FXML
    private void handleGeneratePayslips(ActionEvent event) {
        loadScene("/com/example/payroll/GeneratePayslips.fxml", event, "Payslip Generator");
    }

    @FXML
    private void handleReports(ActionEvent event) {
        loadScene("/com/example/payroll/PayrollReports.fxml", event, "Payroll Reports");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        loadScene("/com/example/payroll/Login.fxml", event, "User Login");
    }

    private void loadScene(String fxmlPath, ActionEvent event, String title) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Could not load " + fxmlPath);
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
