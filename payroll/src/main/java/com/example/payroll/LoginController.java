package com.example.payroll;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button loginBtn;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("Admin", "Employee");
        roleComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Please fill in all fields.");
            return;
        }

        if (role.equals("Admin")) {
            loginAsAdmin(username, password, event);
        } else {
            loginAsEmployee(username, password, event);
        }
    }

    private void loginAsAdmin(String username, String password, ActionEvent event) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND role = 'Admin'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                loadScene("/com/example/payroll/AdminDashboard.fxml", event, "Admin Dashboard");
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid Admin credentials.");
            }
        } catch (SQLException | IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void loginAsEmployee(String username, String password, ActionEvent event) {
        String sql = "SELECT u.*, e.* FROM users u JOIN employees e ON u.name = e.name " +
                "WHERE u.username = ? AND u.password = ? AND u.role = 'Employee'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/payroll/EmployeeDashboard.fxml"));
                Parent root = loader.load();

                EmployeeDashboardController controller = loader.getController();
                controller.setEmployeeData(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("department"),
                        rs.getString("position"),
                        rs.getDouble("basic_salary"),
                        rs.getDouble("working_hours")
                );

                Stage stage = (Stage) loginBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Employee Dashboard");
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid Employee credentials.");
            }
        } catch (SQLException | IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }
    @FXML
    private void handleRegisterLink(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/payroll/Register.fxml")));
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Register");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to load Register page.");
        }
    }

    @FXML
    private void handleHomeLink(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/payroll/Home.fxml")));
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Home");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to load Home page.");
        }
    }


    private void loadScene(String fxmlPath, ActionEvent event, String title) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
        Stage stage = (Stage) loginBtn.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}