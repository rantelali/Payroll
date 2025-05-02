package com.example.payroll;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeRegisterDialogController {
    @FXML private TextField nameField;
    @FXML private TextField departmentField;
    @FXML private TextField positionField;
    @FXML private TextField salaryField;
    @FXML private TextField hoursField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private Stage dialogStage;
    private boolean registered = false;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isRegistered() {
        return registered;
    }

    @FXML
    private void handleRegister() {
        if (validateInput()) {
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);


                String empSql = "INSERT INTO employees (name, department, position, basic_salary, working_hours) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement empStmt = conn.prepareStatement(empSql, PreparedStatement.RETURN_GENERATED_KEYS);
                empStmt.setString(1, nameField.getText());
                empStmt.setString(2, departmentField.getText());
                empStmt.setString(3, positionField.getText());
                empStmt.setDouble(4, Double.parseDouble(salaryField.getText()));
                empStmt.setDouble(5, Double.parseDouble(hoursField.getText()));
                empStmt.executeUpdate();


                int employeeId = 0;
                ResultSet rs = empStmt.getGeneratedKeys();
                if (rs.next()) {
                    employeeId = rs.getInt(1);
                }


                String userSql = "INSERT INTO users (username, password, role, name, email) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement userStmt = conn.prepareStatement(userSql);
                userStmt.setString(1, usernameField.getText());
                userStmt.setString(2, passwordField.getText());
                userStmt.setString(3, "Employee");
                userStmt.setString(4, nameField.getText());
                userStmt.setString(5, emailField.getText());
                userStmt.executeUpdate();

                conn.commit();
                registered = true;
                dialogStage.close();

            } catch (SQLException e) {
                showAlert("Database Error", "Could not register employee: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();

        if (nameField.getText().isEmpty()) {
            errorMessage.append("Name is required!\n");
        }
        if (usernameField.getText().isEmpty()) {
            errorMessage.append("Username is required!\n");
        }
        if (emailField.getText().isEmpty()) {
            errorMessage.append("Email is required!\n");
        }
        if (passwordField.getText().isEmpty()) {
            errorMessage.append("Password is required!\n");
        }
        if (salaryField.getText().isEmpty()) {
            errorMessage.append("Salary is required!\n");
        } else {
            try {
                Double.parseDouble(salaryField.getText());
            } catch (NumberFormatException e) {
                errorMessage.append("Salary must be a valid number!\n");
            }
        }
        if (hoursField.getText().isEmpty()) {
            errorMessage.append("Working hours are required!\n");
        } else {
            try {
                Double.parseDouble(hoursField.getText());
            } catch (NumberFormatException e) {
                errorMessage.append("Working hours must be a valid number!\n");
            }
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            showAlert("Validation Error", errorMessage.toString());
            return false;
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