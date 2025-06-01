package edu.icet.controller;

import edu.icet.Model.Invoice;
import edu.icet.Repocitory.InvoiceRepository;
import edu.icet.Repocitory.impl.InvoiceRepositoryImpl;
import edu.icet.Utill.HibernateUtil;
import edu.icet.Utill.PaymentStatus;
import edu.icet.Utill.ReportGenerator;
import edu.icet.Db.DBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import java.net.URL;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.io.File;
import java.io.IOException;
import java.awt.Desktop;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStream;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import java.sql.DriverManager;

public class InvoiceReportsController implements Initializable {
    @FXML private ComboBox<PaymentStatus> statusFilter;
    @FXML private DatePicker startDate;
    @FXML private DatePicker endDate;
    @FXML private TextField searchField;
    @FXML private TableView<Invoice> invoiceTable;
    @FXML private TableColumn<Invoice, Integer> invoiceIdColumn;
    @FXML private TableColumn<Invoice, LocalDate> dateColumn;
    @FXML private TableColumn<Invoice, String> customerColumn;
    @FXML private TableColumn<Invoice, String> roomColumn;
    @FXML private TableColumn<Invoice, Double> totalAmountColumn;
    @FXML private TableColumn<Invoice, PaymentStatus> statusColumn;
    @FXML private TableColumn<Invoice, Void> actionsColumn;
    @FXML private Label totalInvoicesLabel;
    @FXML private Label totalAmountLabel;

    private final InvoiceRepository invoiceRepository;
    private final ObservableList<Invoice> invoices;

    public InvoiceReportsController() {
        this.invoiceRepository = new InvoiceRepositoryImpl();
        this.invoices = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBox();
        setupTable();
        setupSearch();
        loadInvoices();
    }

    private void setupComboBox() {
        statusFilter.setItems(FXCollections.observableArrayList(PaymentStatus.values()));
    }

    private void setupTable() {
        invoiceIdColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
        customerColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getReservation().getCustomerName()));
        roomColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getReservation().getRoomNumber()));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        
        actionsColumn.setCellFactory(param -> new TableCell<Invoice, Void>() {
            private final Button viewBtn = new Button("View");
            private final Button downloadBtn = new Button("Download");
            private final HBox buttons = new HBox(5, viewBtn, downloadBtn);
            {
                viewBtn.setStyle("-fx-background-color: #0dcaf0; -fx-text-fill: white;");
                downloadBtn.setStyle("-fx-background-color: #198754; -fx-text-fill: white;");
                
                viewBtn.setOnAction(event -> {
                    Invoice invoice = getTableView().getItems().get(getIndex());
                    handleViewInvoice(invoice);
                });
                
                downloadBtn.setOnAction(event -> {
                    Invoice invoice = getTableView().getItems().get(getIndex());
                    handleDownloadInvoice(invoice);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
        
        invoiceTable.setItems(invoices);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                loadInvoices();
            } else {
                // Implement search functionality
                // This could search by invoice ID or customer name
            }
        });
    }

    @FXML
    private void handleSearch() {
        PaymentStatus status = statusFilter.getValue();
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();
        
        List<Invoice> filteredInvoices;
        if (status != null) {
            filteredInvoices = invoiceRepository.findByPaymentStatus(status);
        } else if (start != null && end != null) {
            filteredInvoices = invoiceRepository.findByDateRange(start, end);
        } else {
            loadInvoices();
            return;
        }
        
        invoices.setAll(filteredInvoices);
        updateSummary();
    }

    @FXML
    private void handleReset() {
        statusFilter.setValue(null);
        startDate.setValue(null);
        endDate.setValue(null);
        searchField.clear();
        loadInvoices();
    }

    @FXML
    private void handleDownloadAll() {
        try {
            if (invoices.isEmpty()) {
                showAlert("Warning", "No invoices to download.", Alert.AlertType.WARNING);
                return;
            }

            Connection connection = getConnection();
            if (connection == null) {
                return; // Error already shown in getConnection()
            }

            String startDateStr = startDate.getValue() != null ? startDate.getValue().toString() : null;
            String endDateStr = endDate.getValue() != null ? endDate.getValue().toString() : null;
            String status = statusFilter.getValue() != null ? statusFilter.getValue().toString() : null;
            
            // Create reports directory if it doesn't exist
            File reportsDir = new File("reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdirs();
            }
            
            // Generate the report
            String fileName = String.format("reports/invoice_list_%s.pdf", 
                System.currentTimeMillis());
            
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("START_DATE", startDateStr);
            parameters.put("END_DATE", endDateStr);
            parameters.put("PAYMENT_STATUS", status);
            
            // Get logo path - using file system path instead of URL
            URL logoUrl = getClass().getResource("/images/hotel_logo.png");
            if (logoUrl == null) {
                showAlert("Warning", "Hotel logo not found. Report will be generated without logo.", 
                    Alert.AlertType.WARNING);
                parameters.put("LOGO_PATH", "");
            } else {
                parameters.put("LOGO_PATH", logoUrl.getPath());
            }
            
            // Load and compile the report template
            try (InputStream reportStream = getClass().getResourceAsStream("/reports/invoice_list_report.jrxml")) {
                if (reportStream == null) {
                    showAlert("Error", "Could not find report template file.", Alert.AlertType.ERROR);
                    return;
                }
                
                JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
                
                // Fill the report
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
                
                // Export to PDF
                JasperExportManager.exportReportToPdfFile(jasperPrint, fileName);
                
                showAlert("Success", "Invoice list report has been downloaded to: " + fileName, 
                    Alert.AlertType.INFORMATION);
                
                // Open the PDF file
                openFile(new File(fileName));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to generate report: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleGenerateReport() {
        try {
            Connection connection = getConnection();
            if (connection == null) {
                return; // Error already shown in getConnection()
            }
            
            String startDateStr = startDate.getValue() != null ? startDate.getValue().toString() : null;
            String endDateStr = endDate.getValue() != null ? endDate.getValue().toString() : null;
            
            try {
                ReportGenerator.generateRevenueReport(connection, startDateStr, endDateStr);
                showAlert("Success", "Revenue report has been generated.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to generate report: " + e.getMessage(), Alert.AlertType.ERROR);
            } finally {
                try {
                    connection.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to generate report: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleViewInvoice(Invoice invoice) {
        try {
            Connection connection = getConnection();
            if (connection == null) {
                return; // Error already shown in getConnection()
            }
            
            // Create invoices directory if it doesn't exist
            File invoicesDir = new File("invoices");
            if (!invoicesDir.exists()) {
                invoicesDir.mkdirs();
            }
            
            // Generate the report
            String fileName = String.format("invoices/invoice_%d_%s.pdf", 
                invoice.getInvoiceId(), System.currentTimeMillis());
            
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("INVOICE_ID", invoice.getInvoiceId());
            
            // Handle logo path
            String logoPath = getClass().getResource("/images/hotel_logo.png") != null ? 
                getClass().getResource("/images/hotel_logo.png").getPath() : "";
            parameters.put("LOGO_PATH", logoPath);
            
            // Load and compile the report template
            try (InputStream reportStream = getClass().getResourceAsStream("/reports/invoice_report.jrxml")) {
                if (reportStream == null) {
                    showAlert("Error", "Could not find report template file.", Alert.AlertType.ERROR);
                    return;
                }
                
                JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
                JasperExportManager.exportReportToPdfFile(jasperPrint, fileName);
                
                // Open the PDF file
                openFile(new File(fileName));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to view invoice: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleDownloadInvoice(Invoice invoice) {
        try {
            Connection connection = getConnection();
            if (connection == null) {
                return; // Error already shown in getConnection()
            }
            
            // Create invoices directory if it doesn't exist
            File invoicesDir = new File("invoices");
            if (!invoicesDir.exists()) {
                invoicesDir.mkdirs();
            }
            
            // Generate the report
            String fileName = String.format("invoices/invoice_%d.pdf", invoice.getInvoiceId());
            
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("INVOICE_ID", invoice.getInvoiceId());
            
            // Handle logo path
            String logoPath = getClass().getResource("/images/hotel_logo.png") != null ? 
                getClass().getResource("/images/hotel_logo.png").getPath() : "";
            parameters.put("LOGO_PATH", logoPath);
            
            // Load and compile the report template
            try (InputStream reportStream = getClass().getResourceAsStream("/reports/invoice_report.jrxml")) {
                if (reportStream == null) {
                    showAlert("Error", "Could not find report template file.", Alert.AlertType.ERROR);
                    return;
                }
                
                JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
                JasperExportManager.exportReportToPdfFile(jasperPrint, fileName);
                
                showAlert("Success", "Invoice has been downloaded to: " + fileName, 
                    Alert.AlertType.INFORMATION);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to download invoice: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void openFile(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                showAlert("Warning", "Desktop is not supported. Please open the file manually: " + 
                    file.getAbsolutePath(), Alert.AlertType.WARNING);
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to open file: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadInvoices() {
        List<Invoice> allInvoices = invoiceRepository.findAll();
        invoices.setAll(allInvoices);
        updateSummary();
    }

    private void updateSummary() {
        totalInvoicesLabel.setText(String.valueOf(invoices.size()));
        double total = invoices.stream()
            .mapToDouble(Invoice::getTotalAmount)
            .sum();
        totalAmountLabel.setText(String.format("$%,.2f", total));
    }

    private Connection getConnection() {
        Connection connection = DBConnection.getInstance().getConnection();
        if (connection == null) {
            showAlert("Database Error", 
                "Could not connect to database. Please check your MySQL server is running and credentials are correct.", 
                Alert.AlertType.ERROR);
        }
        return connection;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 