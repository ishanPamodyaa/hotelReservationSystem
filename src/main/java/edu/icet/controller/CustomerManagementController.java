package edu.icet.controller;

import edu.icet.Model.Customer;
import edu.icet.Repocitory.CustomerRepository;
import edu.icet.Repocitory.impl.CustomerRepositoryImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URL;
import java.util.ResourceBundle;

public class CustomerManagementController implements Initializable {

    @FXML
    private TextField nameField;

    @FXML
    private TextField nicField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField addressField;

    @FXML
    private Button saveButton;

    @FXML
    private Button clearButton;

    @FXML
    private Label messageLabel;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Customer> customerTable;

    @FXML
    private TableColumn<Customer, Integer> idColumn;

    @FXML
    private TableColumn<Customer, String> nameColumn;

    @FXML
    private TableColumn<Customer, String> nicColumn;

    @FXML
    private TableColumn<Customer, String> phoneColumn;

    @FXML
    private TableColumn<Customer, Void> actionColumn;

    private final CustomerRepository customerRepository;
    private final ObservableList<Customer> customerList;
    private Customer selectedCustomer;

    public CustomerManagementController() {
        this.customerRepository = new CustomerRepositoryImpl();
        this.customerList = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupSearchField();
        loadCustomers();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nicColumn.setCellValueFactory(new PropertyValueFactory<>("nic"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        
        // Setup action column with Edit and Delete buttons
        actionColumn.setCellFactory(new Callback<TableColumn<Customer, Void>, TableCell<Customer, Void>>() {
            @Override
            public TableCell<Customer, Void> call(final TableColumn<Customer, Void> param) {
                return new TableCell<Customer, Void>() {
                    private final Button editButton = new Button("Edit");
                    private final Button deleteButton = new Button("Delete");
                    private final HBox buttonBox = new HBox(5, editButton, deleteButton);

                    {
                        editButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white;");
                        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                        
                        editButton.setOnAction(event -> {
                            Customer customer = getTableView().getItems().get(getIndex());
                            handleEdit(customer);
                        });
                        
                        deleteButton.setOnAction(event -> {
                            Customer customer = getTableView().getItems().get(getIndex());
                            handleDelete(customer);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : buttonBox);
                    }
                };
            }
        });

        customerTable.setItems(customerList);
    }

    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                loadCustomers();
            } else {
                customerList.setAll(customerRepository.searchByNameOrNic(newValue));
            }
        });
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        Customer customer = new Customer(
            selectedCustomer != null ? selectedCustomer.getCustomerId() : 0,
            nameField.getText().trim(),
            nicField.getText().trim(),
            emailField.getText().trim(),
            phoneField.getText().trim(),
            addressField.getText().trim(),
            null,
            null
        );

        boolean success;
        if (selectedCustomer == null) {
            success = customerRepository.save(customer);
            showMessage(success ? "Customer added successfully" : "Failed to add customer", !success);
        } else {
            success = customerRepository.update(customer);
            showMessage(success ? "Customer updated successfully" : "Failed to update customer", !success);
        }

        if (success) {
            loadCustomers();
            clearFields();
        }
    }

    private boolean validateInputs() {
        String name = nameField.getText().trim();
        String nic = nicField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty()) {
            showMessage("Name is required", true);
            return false;
        }
        if (nic.isEmpty()) {
            showMessage("NIC is required", true);
            return false;
        }
        if (phone.isEmpty()) {
            showMessage("Phone number is required", true);
            return false;
        }

        // Validate email format if provided
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showMessage("Invalid email format", true);
            return false;
        }

        // Validate phone number format (allow +, -, and spaces)
        if (!phone.matches("^[+]?[0-9- ]{10,15}$")) {
            showMessage("Invalid phone number format", true);
            return false;
        }

        // Check if NIC already exists (for new customers)
        if (selectedCustomer == null) {
            Customer existingCustomer = customerRepository.findByNic(nic);
            if (existingCustomer != null) {
                showMessage("A customer with this NIC already exists", true);
                return false;
            }
        }

        return true;
    }

    @FXML
    private void handleClear() {
        clearFields();
    }

    private void clearFields() {
        nameField.clear();
        nicField.clear();
        emailField.clear();
        phoneField.clear();
        addressField.clear();
        messageLabel.setText("");
        selectedCustomer = null;
        saveButton.setText("Save");
    }

    private void handleEdit(Customer customer) {
        selectedCustomer = customer;
        nameField.setText(customer.getName());
        nicField.setText(customer.getNic());
        emailField.setText(customer.getEmail());
        phoneField.setText(customer.getPhone());
        addressField.setText(customer.getAddress());
        saveButton.setText("Update");
    }

    private void handleDelete(Customer customer) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Customer");
        alert.setHeaderText("Delete Customer");
        alert.setContentText("Are you sure you want to delete this customer?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = customerRepository.delete(customer.getCustomerId());
                if (success) {
                    loadCustomers();
                    showMessage("Customer deleted successfully", false);
                } else {
                    showMessage("Failed to delete customer", true);
                }
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadCustomers();
    }

    private void loadCustomers() {
        customerList.setAll(customerRepository.findAll());
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: " + (isError ? "red" : "green"));
    }
}