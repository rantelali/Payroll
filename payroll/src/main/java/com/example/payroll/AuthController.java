package com.example.payroll;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class AuthController {

    @FXML
    protected void handleBack() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/payroll/views/Home.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) getCurrentEventSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    protected void redirectBasedOnRole(String role) throws IOException {
        String fxmlFile = switch (role) {
            case "ADMIN" -> "AdminDashboard.fxml";
            case "EMPLOYEE" -> "EmployeeDashboard.fxml";
            default -> "UserDashboard.fxml";
        };

        Parent root = FXMLLoader.load(getClass().getResource("/com/payroll/views/" + fxmlFile));
        Stage stage = (Stage) ((javafx.scene.Node) getCurrentEventSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    private Object getCurrentEventSource() {
        // This would be implemented based on how you get the event source
        return null; // You'll need to modify this
    }
}