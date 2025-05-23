package edu.icet.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import edu.icet.Utill.PaymentStatus;
import edu.icet.Utill.ReservationStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    private int reservationId;
    private int customerId;
    private int roomId;
    private Customer customer;
    private Room room;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int numGuests;
    private double totalPrice;
    private ReservationStatus status;
    private PaymentStatus paymentStatus;
    private String specialRequests;
    private String createdAt;
    private String updatedAt;
    
    // Additional fields for UI display
    private String customerName;
    private String roomNumber;
    
    // Constructor without ID and timestamps (for new reservations)
    public Reservation(int customerId, int roomId, LocalDate checkInDate, LocalDate checkOutDate, 
                      int numGuests, double totalPrice, ReservationStatus status, 
                      PaymentStatus paymentStatus, String specialRequests) {
        this.customerId = customerId;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numGuests = numGuests;
        this.totalPrice = totalPrice;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.specialRequests = specialRequests;
    }
} 