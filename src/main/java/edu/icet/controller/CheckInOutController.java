package edu.icet.controller;

import edu.icet.Model.Reservation;
import edu.icet.Repocitory.ReservationRepository;
import edu.icet.Repocitory.impl.ReservationRepositoryImpl;
import edu.icet.Utill.ReservationStatus;
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

public class CheckInOutController implements Initializable {
    @FXML private TextField checkInReservationField;
    @FXML private TextField checkOutReservationField;
    @FXML private Label checkInDetailsLabel;
    @FXML private Label checkOutDetailsLabel;
    @FXML private Button checkInButton;
    @FXML private Button checkOutButton;
    @FXML private ComboBox<ReservationStatus> filterStatusComboBox;
    @FXML private TextField searchField;
    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, Integer> idColumn;
    @FXML private TableColumn<Reservation, String> customerColumn;
    @FXML private TableColumn<Reservation, String> roomColumn;
    @FXML private TableColumn<Reservation, LocalDate> checkInDateColumn;
    @FXML private TableColumn<Reservation, LocalDate> checkOutDateColumn;
    @FXML private TableColumn<Reservation, ReservationStatus> statusColumn;
    @FXML private TableColumn<Reservation, Void> actionColumn;

    private final ReservationRepository reservationRepository;
    private final ObservableList<Reservation> reservations;
    private Reservation selectedCheckInReservation;
    private Reservation selectedCheckOutReservation;

    public CheckInOutController() {
        this.reservationRepository = new ReservationRepositoryImpl();
        this.reservations = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupFilterComboBox();
        setupSearchField();
        setupButtons();
        loadReservations();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        customerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomerName()));
        roomColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoomNumber()));
        checkInDateColumn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        checkOutDateColumn.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        actionColumn.setCellFactory(param -> new TableCell<Reservation, Void>() {
            private final Button checkInBtn = new Button("Check In");
            private final Button checkOutBtn = new Button("Check Out");
            private final HBox buttons = new HBox(5, checkInBtn, checkOutBtn);

            {
                checkInBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                checkOutBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

                checkInBtn.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    handleQuickCheckIn(reservation);
                });

                checkOutBtn.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    handleQuickCheckOut(reservation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    checkInBtn.setVisible(reservation.getStatus() == ReservationStatus.Confirmed);
                    checkOutBtn.setVisible(reservation.getStatus() == ReservationStatus.CheckedIn);
                    setGraphic(buttons);
                }
            }
        });

        reservationTable.setItems(reservations);
    }

    private void setupFilterComboBox() {
        filterStatusComboBox.setItems(FXCollections.observableArrayList(ReservationStatus.values()));
        filterStatusComboBox.getItems().add(0, null);
        filterStatusComboBox.setPromptText("All Reservations");
        filterStatusComboBox.setOnAction(e -> handleFilter());
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

    private void setupButtons() {
        checkInButton.setDisable(true);
        checkOutButton.setDisable(true);
    }

    @FXML
    private void handleSearchCheckIn() {
        try {
            int reservationId = Integer.parseInt(checkInReservationField.getText());
            selectedCheckInReservation = reservationRepository.findById(reservationId);
            
            if (selectedCheckInReservation != null) {
                if (selectedCheckInReservation.getStatus() == ReservationStatus.Confirmed) {
                    checkInDetailsLabel.setText(String.format(
                        "Customer: %s\nRoom: %s\nCheck-in Date: %s",
                        selectedCheckInReservation.getCustomerName(),
                        selectedCheckInReservation.getRoomNumber(),
                        selectedCheckInReservation.getCheckInDate()
                    ));
                    checkInButton.setDisable(false);
                } else {
                    checkInDetailsLabel.setText("Reservation is not in Confirmed status");
                    checkInButton.setDisable(true);
                }
            } else {
                checkInDetailsLabel.setText("Reservation not found");
                checkInButton.setDisable(true);
            }
        } catch (NumberFormatException e) {
            checkInDetailsLabel.setText("Please enter a valid reservation ID");
            checkInButton.setDisable(true);
        }
    }

    @FXML
    private void handleSearchCheckOut() {
        try {
            int reservationId = Integer.parseInt(checkOutReservationField.getText());
            selectedCheckOutReservation = reservationRepository.findById(reservationId);
            
            if (selectedCheckOutReservation != null) {
                if (selectedCheckOutReservation.getStatus() == ReservationStatus.CheckedIn) {
                    checkOutDetailsLabel.setText(String.format(
                        "Customer: %s\nRoom: %s\nCheck-out Date: %s",
                        selectedCheckOutReservation.getCustomerName(),
                        selectedCheckOutReservation.getRoomNumber(),
                        selectedCheckOutReservation.getCheckOutDate()
                    ));
                    checkOutButton.setDisable(false);
                } else {
                    checkOutDetailsLabel.setText("Reservation is not in Checked-In status");
                    checkOutButton.setDisable(true);
                }
            } else {
                checkOutDetailsLabel.setText("Reservation not found");
                checkOutButton.setDisable(true);
            }
        } catch (NumberFormatException e) {
            checkOutDetailsLabel.setText("Please enter a valid reservation ID");
            checkOutButton.setDisable(true);
        }
    }

    @FXML
    private void handleCheckIn() {
        if (selectedCheckInReservation != null) {
            handleQuickCheckIn(selectedCheckInReservation);
        }
    }

    @FXML
    private void handleCheckOut() {
        if (selectedCheckOutReservation != null) {
            handleQuickCheckOut(selectedCheckOutReservation);
        }
    }

    private void handleQuickCheckIn(Reservation reservation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Check-in Confirmation");
        alert.setHeaderText("Check-in Reservation #" + reservation.getReservationId());
        alert.setContentText("Are you sure you want to check in this reservation?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            reservation.setStatus(ReservationStatus.CheckedIn);
            boolean success = reservationRepository.update(reservation);
            
            if (success) {
                showMessage("Check-in successful", false);
                loadReservations();
                clearCheckInFields();
            } else {
                showMessage("Failed to check in", true);
            }
        }
    }

    private void handleQuickCheckOut(Reservation reservation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Check-out Confirmation");
        alert.setHeaderText("Check-out Reservation #" + reservation.getReservationId());
        alert.setContentText("Are you sure you want to check out this reservation?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            reservation.setStatus(ReservationStatus.CheckedOut);
            boolean success = reservationRepository.update(reservation);
            
            if (success) {
                showMessage("Check-out successful", false);
                loadReservations();
                clearCheckOutFields();
            } else {
                showMessage("Failed to check out", true);
            }
        }
    }

    private void handleFilter() {
        ReservationStatus selectedStatus = filterStatusComboBox.getValue();
        if (selectedStatus == null) {
            loadReservations();
        } else {
            List<Reservation> filteredReservations = reservationRepository.findByStatus(selectedStatus);
            reservations.setAll(filteredReservations);
        }
    }

    @FXML
    private void handleRefresh() {
        loadReservations();
        clearCheckInFields();
        clearCheckOutFields();
    }

    private void loadReservations() {
        List<Reservation> allReservations = reservationRepository.findAll();
        reservations.setAll(allReservations);
    }

    private void clearCheckInFields() {
        checkInReservationField.clear();
        checkInDetailsLabel.setText("");
        checkInButton.setDisable(true);
        selectedCheckInReservation = null;
    }

    private void clearCheckOutFields() {
        checkOutReservationField.clear();
        checkOutDetailsLabel.setText("");
        checkOutButton.setDisable(true);
        selectedCheckOutReservation = null;
    }

    private void showMessage(String message, boolean isError) {
        Alert alert = new Alert(isError ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle(isError ? "Error" : "Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 