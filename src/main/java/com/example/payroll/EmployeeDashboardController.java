package com.example.payroll;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class EmployeeDashboardController {
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label departmentLabel;
    @FXML private Label positionLabel;
    @FXML private Label salaryLabel;
    @FXML private Label hoursLabel;
    @FXML private Label netSalaryLabel;

    private String currentUsername;

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
        loadEmployeeData();
    }

    public void setEmployeeData(String name, String email, String department,
                                String position, double salary, double hours) {
        nameLabel.setText(name);
        emailLabel.setText(email);
        departmentLabel.setText(department);
        positionLabel.setText(position);
        salaryLabel.setText(String.format("$%.2f", salary));
        hoursLabel.setText(String.format("%.2f hours", hours));

        double netSalary = calculateNetSalary(name);
        netSalaryLabel.setText(String.format("$%.2f", netSalary));
    }

    private void loadEmployeeData() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT e.*, u.email FROM employees e JOIN users u ON e.name = u.name WHERE u.username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentUsername);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                setEmployeeData(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("department"),
                        rs.getString("position"),
                        rs.getDouble("basic_salary"),
                        rs.getDouble("working_hours")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private double calculateNetSalary(String employeeName) {
        try (Connection conn = DBConnection.getConnection()) {
            String salarySql = "SELECT basic_salary FROM employees WHERE name = ?";
            PreparedStatement salaryStmt = conn.prepareStatement(salarySql);
            salaryStmt.setString(1, employeeName);
            ResultSet salaryRs = salaryStmt.executeQuery();

            if (salaryRs.next()) {
                double basicSalary = salaryRs.getDouble("basic_salary");

                String deductionSql = "SELECT (tax + insurance + other) as total_deductions FROM deductions WHERE employee_id = " +
                        "(SELECT employee_id FROM employees WHERE name = ?)";
                PreparedStatement deductionStmt = conn.prepareStatement(deductionSql);
                deductionStmt.setString(1, employeeName);
                ResultSet deductionRs = deductionStmt.executeQuery();

                double deductions = 0.0;
                if (deductionRs.next()) {
                    deductions = deductionRs.getDouble("total_deductions");
                }

                return basicSalary - deductions;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @FXML
    private void handleDownloadPayslip() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Payslip");
            fileChooser.setInitialFileName("payslip_" + currentUsername + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            File file = fileChooser.showSaveDialog(netSalaryLabel.getScene().getWindow());

            if (file != null) {
                generatePdfPayslip(file);
                showAlert("Success", "Payslip generated successfully at: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to generate payslip: " + e.getMessage());
        }
    }

    private void generatePdfPayslip(File file) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        Font companyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLUE);
        Paragraph company = new Paragraph("XYZ Company\n", companyFont);
        company.setAlignment(Element.ALIGN_CENTER);
        document.add(company);

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Paragraph title = new Paragraph("PAYSLIP\n\n", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        Paragraph datePara = new Paragraph("Date: " + date + "\n\n", dateFont);
        document.add(datePara);

        Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        document.add(new Paragraph("Employee Details:", headingFont));
        document.add(new Paragraph("Name: " + nameLabel.getText(), valueFont));
        document.add(new Paragraph("Email: " + emailLabel.getText(), valueFont));
        document.add(new Paragraph("Department: " + departmentLabel.getText(), valueFont));
        document.add(new Paragraph("Position: " + positionLabel.getText(), valueFont));
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("Payment Details:", headingFont));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        table.addCell(new Phrase("Description", headingFont));
        table.addCell(new Phrase("Amount ($)", headingFont));

        table.addCell(new Phrase("Basic Salary", valueFont));
        table.addCell(new Phrase(salaryLabel.getText(), valueFont));

        table.addCell(new Phrase("Tax Deduction", valueFont));
        table.addCell(new Phrase("-50.00", valueFont));

        table.addCell(new Phrase("Insurance", valueFont));
        table.addCell(new Phrase("-30.00", valueFont));

        PdfPCell cell = new PdfPCell(new Phrase("NET PAY", headingFont));
        cell.setColspan(2);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        table.addCell(new Phrase(""));
        table.addCell(new Phrase(netSalaryLabel.getText(), headingFont));

        document.add(table);

        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10);
        Paragraph footer = new Paragraph(
                "\n\nThis is a computer generated payslip. No signature required.", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/payroll/Login.fxml")));
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}