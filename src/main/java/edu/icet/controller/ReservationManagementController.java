package edu.icet.controller;

import edu.icet.Model.Customer;
import edu.icet.Model.Reservation;
import edu.icet.Model.Room;
import edu.icet.Utill.RoomType;
import edu.icet.Repocitory.CustomerRepository;
import edu.icet.Repocitory.ReservationRepository;
import edu.icet.Repocitory.RoomRepository;
import edu.icet.Repocitory.impl.CustomerRepositoryImpl;
import edu.icet.Repocitory.impl.ReservationRepositoryImpl;
import edu.icet.Repocitory.impl.RoomRepositoryImpl;
import edu.icet.Utill.PaymentStatus;
import edu.icet.Utill.ReservationStatus;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ReservationManagementController implements Initializable {
    @FXML private ComboBox<Customer> customerComboBox;
    @FXML private ComboBox<Room> roomComboBox;
    @FXML private DatePicker checkInDatePicker;
    @FXML private DatePicker checkOutDatePicker;
    @FXML private TextField numGuestsField;
    @FXML private TextField totalPriceField;
    @FXML private ComboBox<ReservationStatus> statusComboBox;
    @FXML private ComboBox<PaymentStatus> paymentStatusComboBox;
    @FXML private TextArea specialRequestsArea;
    @FXML private Label messageLabel;
    @FXML private TextField searchField;
    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, Integer> idColumn;
    @FXML private TableColumn<Reservation, String> customerColumn;
    @FXML private TableColumn<Reservation, String> roomColumn;
    @FXML private TableColumn<Reservation, String> roomTypeColumn;
    @FXML private TableColumn<Reservation, Double> priceColumn;
    @FXML private TableColumn<Reservation, LocalDate> checkInColumn;
    @FXML private TableColumn<Reservation, LocalDate> checkOutColumn;
    @FXML private TableColumn<Reservation, ReservationStatus> statusColumn;
    @FXML private TableColumn<Reservation, Void> actionColumn;
    @FXML private ComboBox<RoomType> roomTypeComboBox;

    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;
    private final RoomRepository roomRepository;
    private Reservation selectedReservation;
    private final ObservableList<Reservation> reservations;

    public ReservationManagementController() {
        this.reservationRepository = new ReservationRepositoryImpl();
        this.customerRepository = new CustomerRepositoryImpl();
        this.roomRepository = new RoomRepositoryImpl();
        this.reservations = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupTableColumns();
        setupSearchField();
        setupDatePickers();
        loadReservations();
        
        // Add listener to numGuestsField for numeric validation
        numGuestsField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                numGuestsField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Add listener to totalPriceField for numeric validation
        totalPriceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                totalPriceField.setText(oldValue);
            }
        });

        // Add listener for room selection to update total price
        roomComboBox.setOnAction(event -> {
            Room selectedRoom = roomComboBox.getValue();
            if (selectedRoom != null && checkInDatePicker.getValue() != null && checkOutDatePicker.getValue() != null) {
                updateTotalPrice();
            }
        });

        // Add listeners to date pickers to update total price and check availability
        checkInDatePicker.setOnAction(event -> {
            if (checkOutDatePicker.getValue() != null) {
                validateDates();
                updateTotalPrice();
            }
        });

        checkOutDatePicker.setOnAction(event -> {
            if (checkInDatePicker.getValue() != null) {
                validateDates();
                updateTotalPrice();
            }
        });
    }

    private void setupComboBoxes() {
        // Setup customer combo box
        customerComboBox.setItems(FXCollections.observableArrayList(customerRepository.findAll()));
        customerComboBox.setConverter(new StringConverter<Customer>() {
            @Override
            public String toString(Customer customer) {
                return customer == null ? "" : customer.getName();
            }

            @Override
            public Customer fromString(String string) {
                return null;
            }
        });

        // Setup room type combo box
        roomTypeComboBox.setItems(FXCollections.observableArrayList(RoomType.values()));
        roomTypeComboBox.setOnAction(e -> updateRoomComboBox());

        // Setup room combo box
        roomComboBox.setConverter(new StringConverter<Room>() {
            @Override
            public String toString(Room room) {
                return room == null ? "" : "Room " + room.getRoomNumber();
            }

            @Override
            public Room fromString(String string) {
                return null;
            }
        });

        // Setup status combo box
        statusComboBox.setItems(FXCollections.observableArrayList(ReservationStatus.values()));

        // Setup payment status combo box
        paymentStatusComboBox.setItems(FXCollections.observableArrayList(PaymentStatus.values()));
    }

    private void updateRoomComboBox() {
        RoomType selectedType = roomTypeComboBox.getValue();
        if (selectedType != null) {
            List<Room> rooms = roomRepository.findByType(selectedType);
            roomComboBox.setItems(FXCollections.observableArrayList(rooms));
        } else {
            roomComboBox.getItems().clear();
        }
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        
        customerColumn.setCellValueFactory(cellData -> {
            Reservation reservation = cellData.getValue();
            System.out.println("Reservation: " + reservation);
            return new SimpleStringProperty(reservation != null ? reservation.getCustomerName() : "");
        });
        
        roomColumn.setCellValueFactory(cellData -> {
            Reservation reservation = cellData.getValue();
           
            return new SimpleStringProperty(reservation != null ? "Room " + reservation.getRoomNumber() : "");
        });

        roomTypeColumn.setCellValueFactory(cellData -> {
            Reservation reservation = cellData.getValue();
            Room room = reservation.getRoom();
            return new SimpleStringProperty(room != null ? room.getRoomType().toString() : "");
        });

        priceColumn.setCellValueFactory(cellData -> {
            Reservation reservation = cellData.getValue();
            Room room = reservation.getRoom();
            if (room != null) {
                return new SimpleObjectProperty<>(room.getPricePerNight());
            }
            return new SimpleObjectProperty<>(0.0);
        });

        checkInColumn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        checkOutColumn.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Setup action column with edit and delete buttons
        actionColumn.setCellFactory(new Callback<TableColumn<Reservation, Void>, TableCell<Reservation, Void>>() {
            @Override
            public TableCell<Reservation, Void> call(TableColumn<Reservation, Void> param) {
                return new TableCell<Reservation, Void>() {
                    private final Button editButton = new Button("Edit");
                    private final Button deleteButton = new Button("Delete");
                    private final HBox buttons = new HBox(5, editButton, deleteButton);

                    {
                        editButton.setOnAction(event -> {
                            Reservation reservation = getTableView().getItems().get(getIndex());
                            if (reservation != null) {
                                handleEdit(reservation);
                            }
                        });

                        deleteButton.setOnAction(event -> {
                            Reservation reservation = getTableView().getItems().get(getIndex());
                            if (reservation != null) {
                                handleDelete(reservation);
                            }
                        });

                        // Style buttons
                        editButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white;");
                        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : buttons);
                    }
                };
            }
        });

        reservationTable.setItems(reservations);
    }

    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                loadReservations();
            } else {
                List<Reservation> searchResults = reservationRepository.searchReservations(newValue);
                reservations.setAll(searchResults);
            }
        });
    }

    private void setupDatePickers() {
        // Disable past dates
        checkInDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        checkOutDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
    }

    private void validateDates() {
        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = checkOutDatePicker.getValue();

        if (checkIn != null && checkOut != null) {
            if (checkOut.isBefore(checkIn)) {
                checkOutDatePicker.setValue(null);
                showMessage("Check-out date cannot be before check-in date", true);
            } else if (checkIn.isBefore(LocalDate.now())) {
                checkInDatePicker.setValue(null);
                showMessage("Check-in date cannot be in the past", true);
            }
        }
    }

    private void updateTotalPrice() {
        Room selectedRoom = roomComboBox.getValue();
        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = checkOutDatePicker.getValue();

        if (selectedRoom != null && checkIn != null && checkOut != null) {
            long days = checkIn.until(checkOut).getDays();
            double totalPrice = selectedRoom.getPricePerNight() * days;
            totalPriceField.setText(String.format("%.2f", totalPrice));
        }
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        try {
            Reservation reservation = new Reservation();
            if (selectedReservation != null) {
                reservation.setReservationId(selectedReservation.getReservationId());
            }

            reservation.setCustomer(customerComboBox.getValue());
            System.out.println("Customer: " + reservation.getCustomer());
            reservation.setRoom(roomComboBox.getValue());
            System.out.println("Room: " + reservation.getRoom());
            reservation.setCheckInDate(checkInDatePicker.getValue());
            System.out.println("Check-in Date: " + reservation.getCheckInDate());
            reservation.setCheckOutDate(checkOutDatePicker.getValue());
            System.out.println("Check-out Date: " + reservation.getCheckOutDate());
            reservation.setNumGuests(Integer.parseInt(numGuestsField.getText()));
            System.out.println("Number of Guests: " + reservation.getNumGuests());
            reservation.setTotalPrice(Double.parseDouble(totalPriceField.getText()));
            System.out.println("Total Price: " + reservation.getTotalPrice());
            reservation.setStatus(statusComboBox.getValue());
            System.out.println("Status: " + reservation.getStatus());
            reservation.setPaymentStatus(paymentStatusComboBox.getValue());
            System.out.println("Payment Status: " + reservation.getPaymentStatus());
            reservation.setSpecialRequests(specialRequestsArea.getText());
            System.out.println("Special Requests: " + reservation.getSpecialRequests());

            System.out.println("Reservation: " + reservation);

            // Check room availability
            if (!reservationRepository.isRoomAvailable(
                    reservation.getRoom().getRoomId(),
                    reservation.getCheckInDate(),
                    reservation.getCheckOutDate())) {
                showMessage("Room is not available for the selected dates", true);
                return;
            }

            boolean success;
            if (selectedReservation == null) {
                success = reservationRepository.save(reservation);
            } else {
                success = reservationRepository.update(reservation);
            }

            if (success) {
                showMessage("Reservation " + (selectedReservation == null ? "created" : "updated") + " successfully", false);
                handleClear();
                loadReservations();
            } else {
                showMessage("Failed to " + (selectedReservation == null ? "create" : "update") + " reservation", true);
            }
        } catch (Exception e) {
            showMessage("Error: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleClear() {
        selectedReservation = null;
        customerComboBox.setValue(null);
        roomComboBox.setValue(null);
        checkInDatePicker.setValue(null);
        checkOutDatePicker.setValue(null);
        numGuestsField.clear();
        totalPriceField.clear();
        statusComboBox.setValue(null);
        paymentStatusComboBox.setValue(null);
        specialRequestsArea.clear();
        messageLabel.setText("");
    }

   

    private void handleEdit(Reservation reservation) {
        selectedReservation = reservation;
        customerComboBox.setValue(reservation.getCustomer());
        roomComboBox.setValue(reservation.getRoom());
        checkInDatePicker.setValue(reservation.getCheckInDate());
        checkOutDatePicker.setValue(reservation.getCheckOutDate());
        numGuestsField.setText(String.valueOf(reservation.getNumGuests()));
        totalPriceField.setText(String.format("%.2f", reservation.getTotalPrice()));
        statusComboBox.setValue(reservation.getStatus());
        paymentStatusComboBox.setValue(reservation.getPaymentStatus());
        specialRequestsArea.setText(reservation.getSpecialRequests());
    }

    private void handleDelete(Reservation reservation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Reservation");
        alert.setHeaderText("Delete Reservation #" + reservation.getReservationId());
        alert.setContentText("Are you sure you want to delete this reservation?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = reservationRepository.delete(reservation.getReservationId());
            if (success) {
                showMessage("Reservation deleted successfully", false);
                loadReservations();
            } else {
                showMessage("Failed to delete reservation", true);
            }
        }
    }

    private boolean validateForm() {
        if (customerComboBox.getValue() == null) {
            showMessage("Please select a customer", true);
            return false;
        }
        if (roomComboBox.getValue() == null) {
            showMessage("Please select a room", true);
            return false;
        }
        if (checkInDatePicker.getValue() == null) {
            showMessage("Please select a check-in date", true);
            return false;
        }
        if (checkOutDatePicker.getValue() == null) {
            showMessage("Please select a check-out date", true);
            return false;
        }
        if (numGuestsField.getText().isEmpty()) {
            showMessage("Please enter number of guests", true);
            return false;
        }
        if (totalPriceField.getText().isEmpty()) {
            showMessage("Please enter total price", true);
            return false;
        }
        if (statusComboBox.getValue() == null) {
            showMessage("Please select a reservation status", true);
            return false;
        }
        if (paymentStatusComboBox.getValue() == null) {
            showMessage("Please select a payment status", true);
            return false;
        }
        return true;
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: " + (isError ? "red" : "green"));
    }

    private void loadReservations() {
        List<Reservation> allReservations = reservationRepository.findAll();
        reservations.setAll(allReservations);
    }
    @FXML
    public void handleRefresh(javafx.event.ActionEvent event) {
        loadReservations();
        messageLabel.setText("Reservations refreshed.");
    }
} 
