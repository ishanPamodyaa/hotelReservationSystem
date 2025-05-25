package edu.icet.controller;

import edu.icet.Model.*;
import edu.icet.Repocitory.InvoiceRepository;
import edu.icet.Repocitory.ReservationRepository;
import edu.icet.Repocitory.impl.InvoiceRepositoryImpl;
import edu.icet.Repocitory.impl.ReservationRepositoryImpl;
import edu.icet.Utill.PaymentStatus;
import edu.icet.Utill.InvoicePrinter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BillingManagementController implements Initializable {
    @FXML private TextField reservationIdField;
    @FXML private TextField customerField;
    @FXML private TextField roomField;
    @FXML private TextField roomChargesField;
    @FXML private TextField additionalChargesField;
    @FXML private TextField taxAmountField;
    @FXML private TextField totalAmountField;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private ComboBox<PaymentStatus> paymentStatusComboBox;
    @FXML private TextArea  notesField;
    
    @FXML private TextField itemDescriptionField;
    @FXML private TextField itemQuantityField;
    @FXML private TextField itemUnitPriceField;
    @FXML private TableView<InvoiceItem> itemsTable;
    @FXML private TableColumn<InvoiceItem, String> itemDescriptionColumn;
    @FXML private TableColumn<InvoiceItem, Integer> itemQuantityColumn;
    @FXML private TableColumn<InvoiceItem, Double> itemUnitPriceColumn;
    @FXML private TableColumn<InvoiceItem, Double> itemAmountColumn;
    @FXML private TableColumn<InvoiceItem, Void> itemActionColumn;
    
    @FXML private ComboBox<PaymentStatus> filterStatusComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField searchField;
    @FXML private TableView<Invoice> invoiceTable;
    @FXML private TableColumn<Invoice, Integer> invoiceIdColumn;
    @FXML private TableColumn<Invoice, String> customerNameColumn;
    @FXML private TableColumn<Invoice, String> roomNumberColumn;
    @FXML private TableColumn<Invoice, LocalDate> invoiceDateColumn;
    @FXML private TableColumn<Invoice, Double> totalAmountColumn;
    @FXML private TableColumn<Invoice, PaymentStatus> paymentStatusColumn;
    @FXML private TableColumn<Invoice, Void> actionColumn;

    private final InvoiceRepository invoiceRepository;
    private final ReservationRepository reservationRepository;
    private final ObservableList<InvoiceItem> invoiceItems;
    private final ObservableList<Invoice> invoices;
    private Reservation currentReservation;

    public BillingManagementController() {
        this.invoiceRepository = new InvoiceRepositoryImpl();
        this.reservationRepository = new ReservationRepositoryImpl();
        this.invoiceItems = FXCollections.observableArrayList();
        this.invoices = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupItemsTable();
        setupInvoiceTable();
        setupListeners();
        loadInvoices();
    }

    private void setupComboBoxes() {
        paymentMethodComboBox.setItems(FXCollections.observableArrayList(
            "Cash", "Credit Card", "Debit Card", "Bank Transfer", "Online Payment"
        ));
        
        paymentStatusComboBox.setItems(FXCollections.observableArrayList(PaymentStatus.values()));
        filterStatusComboBox.setItems(FXCollections.observableArrayList(PaymentStatus.values()));
    }

    private void setupItemsTable() {
        itemDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        itemQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        itemUnitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        itemAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        
        itemActionColumn.setCellFactory(param -> new TableCell<InvoiceItem, Void>() {
            private final Button deleteBtn = new Button("Delete");
            {
                deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                deleteBtn.setOnAction(event -> {
                    InvoiceItem item = getTableView().getItems().get(getIndex());
                    invoiceItems.remove(item);
                    updateTotalAmount();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });
        
        itemsTable.setItems(invoiceItems);
    }

    private void setupInvoiceTable() {
        invoiceIdColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        customerNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getReservation().getCustomerName()));
        roomNumberColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getReservation().getRoomNumber()));
        invoiceDateColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        paymentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        
        actionColumn.setCellFactory(param -> new TableCell<Invoice, Void>() {
            private final Button viewBtn = new Button("View");
            private final Button printBtn = new Button("Print");
            private final HBox buttons = new HBox(5, viewBtn, printBtn);
            {
                viewBtn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");
                printBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
                
                viewBtn.setOnAction(event -> {
                    Invoice invoice = getTableView().getItems().get(getIndex());
                    handleViewInvoice(invoice);
                });
                
                printBtn.setOnAction(event -> {
                    Invoice invoice = getTableView().getItems().get(getIndex());
                    handlePrintInvoice(invoice);
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

    private void setupListeners() {
        roomChargesField.textProperty().addListener((obs, oldVal, newVal) -> updateTotalAmount());
        additionalChargesField.textProperty().addListener((obs, oldVal, newVal) -> updateTotalAmount());
        taxAmountField.textProperty().addListener((obs, oldVal, newVal) -> updateTotalAmount());
        
        filterStatusComboBox.setOnAction(e -> handleFilter());
        startDatePicker.setOnAction(e -> handleFilter());
        endDatePicker.setOnAction(e -> handleFilter());
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                loadInvoices();
            } else {
                // Implement search functionality
            }
        });
    }

    @FXML
    private void handleSearchReservation() {
        try {
            int reservationId = Integer.parseInt(reservationIdField.getText());
            currentReservation = reservationRepository.findById(reservationId);
            
            if (currentReservation != null) {
                customerField.setText(currentReservation.getCustomerName());
                roomField.setText(currentReservation.getRoomNumber());
                roomChargesField.setText(String.valueOf(currentReservation.getTotalPrice()));
                updateTotalAmount();
            } else {
                showAlert("Error", "Reservation not found", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid reservation ID", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAddItem() {
        try {
            String description = itemDescriptionField.getText();
            int quantity = Integer.parseInt(itemQuantityField.getText());
            double unitPrice = Double.parseDouble(itemUnitPriceField.getText());
            
            InvoiceItem item = new InvoiceItem(description, quantity, unitPrice);
            invoiceItems.add(item);
            
            itemDescriptionField.clear();
            itemQuantityField.clear();
            itemUnitPriceField.clear();
            
            updateTotalAmount();
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers for quantity and unit price", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSave() {
            // System.out.println("reservation id "+ currentReservation.getReservationId());
            // System.out.println("invoice "+ invoiceRepository.findByReservationId(currentReservation.getReservationId()));
        
            // System.out.println(" Available ? "+ invoiceRepository.isAvailableReservationAtInvoice(currentReservation.getReservationId()));

        if (currentReservation == null) {
            showAlert("Error", "Please select a reservation first", Alert.AlertType.ERROR);
            return;
        }

        if(invoiceRepository.isAvailableReservationAtInvoice(currentReservation.getReservationId())){
            showAlert("Error", "Invoice already exists for this reservation", Alert.AlertType.ERROR);
            return;
        }
        
        if (paymentMethodComboBox.getValue() == null || paymentStatusComboBox.getValue() == null) {
            showAlert("Error", "Please select payment method and status", Alert.AlertType.ERROR);
            return;
        }
        
        try {
            double roomCharges = Double.parseDouble(roomChargesField.getText());
            double additionalCharges = Double.parseDouble(additionalChargesField.getText());
            double taxAmount = Double.parseDouble(taxAmountField.getText());
            
            Invoice invoice = new Invoice(
                currentReservation,
                LocalDate.now(),
                roomCharges,
                additionalCharges,
                taxAmount,
                paymentStatusComboBox.getValue()
            );
            
            invoice.setPaymentMethod(paymentMethodComboBox.getValue());
            invoice.setNotes(notesField.getText());
            invoice.setItems(invoiceItems);
            
            if (invoiceRepository.save(invoice)) {
                showAlert("Success", "Invoice saved successfully", Alert.AlertType.INFORMATION);
                handleClear();
                loadInvoices();
            } else {
                showAlert("Error", "Failed to save invoice", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers for charges", Alert.AlertType.ERROR);
        }
    
    }

    @FXML
    private void handleClear() {
        reservationIdField.clear();
        customerField.clear();
        roomField.clear();
        roomChargesField.clear();
        additionalChargesField.setText("0.00");
        taxAmountField.setText("0.00");
        totalAmountField.clear();
        paymentMethodComboBox.setValue(null);
        paymentStatusComboBox.setValue(null);
        notesField.clear();
        invoiceItems.clear();
        currentReservation = null;
    }

    @FXML
    private void handleRefresh() {
        loadInvoices();
    }

    @FXML
    private void handlePrint() {
        if (currentReservation == null) {
            showAlert("Error", "Please create and save the invoice first", Alert.AlertType.ERROR);
            return;
        }
        
        try {
            double roomCharges = Double.parseDouble(roomChargesField.getText());
            double additionalCharges = Double.parseDouble(additionalChargesField.getText());
            double taxAmount = Double.parseDouble(taxAmountField.getText());
            
            Invoice invoice = new Invoice(
                currentReservation,
                LocalDate.now(),
                roomCharges,
                additionalCharges,
                taxAmount,
                paymentStatusComboBox.getValue()
            );
            
            invoice.setPaymentMethod(paymentMethodComboBox.getValue());
            invoice.setNotes(notesField.getText());
            invoice.setItems(invoiceItems);
            
            InvoicePrinter.printInvoice(invoice);
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers for charges", Alert.AlertType.ERROR);
        }
    }

    private void handleViewInvoice(Invoice invoice) {
        // Populate form fields with invoice data
        currentReservation = invoice.getReservation();
        reservationIdField.setText(String.valueOf(currentReservation.getReservationId()));
        customerField.setText(currentReservation.getCustomerName());
        roomField.setText(currentReservation.getRoomNumber());
        roomChargesField.setText(String.valueOf(invoice.getRoomCharges()));
        additionalChargesField.setText(String.valueOf(invoice.getAdditionalCharges()));
        taxAmountField.setText(String.valueOf(invoice.getTaxAmount()));
        totalAmountField.setText(String.valueOf(invoice.getTotalAmount()));
        paymentMethodComboBox.setValue(invoice.getPaymentMethod());
        paymentStatusComboBox.setValue(invoice.getPaymentStatus());
        notesField.setText(invoice.getNotes());
        
        // Update invoice items table
        invoiceItems.clear();
        invoiceItems.addAll(invoice.getItems());
    }

    private void handlePrintInvoice(Invoice invoice) {
        InvoicePrinter.printInvoice(invoice);
    }

    private void handleFilter() {
        PaymentStatus status = filterStatusComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (status != null) {
            List<Invoice> filteredInvoices = invoiceRepository.findByPaymentStatus(status);
            invoices.setAll(filteredInvoices);
        } else if (startDate != null && endDate != null) {
            List<Invoice> filteredInvoices = invoiceRepository.findByDateRange(startDate, endDate);
            invoices.setAll(filteredInvoices);
        } else {
            loadInvoices();
        }
    }

    private void loadInvoices() {
        List<Invoice> allInvoices = invoiceRepository.findAll();
        invoices.setAll(allInvoices);
    }

    private void updateTotalAmount() {
        try {
            double roomCharges = Double.parseDouble(roomChargesField.getText());
            double additionalCharges = Double.parseDouble(additionalChargesField.getText());
            double taxAmount = Double.parseDouble(taxAmountField.getText());
            double itemsTotal = invoiceItems.stream()
                .mapToDouble(InvoiceItem::getAmount)
                .sum();
            additionalChargesField.setText(String.format("%.2f",itemsTotal ));
            
            double total = roomCharges + additionalCharges + taxAmount + itemsTotal;
            
            totalAmountField.setText(String.format("%.2f", total));
        } catch (NumberFormatException e) {
            totalAmountField.setText("0.00");
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 