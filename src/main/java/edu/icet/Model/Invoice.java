package edu.icet.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import edu.icet.Utill.PaymentStatus;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    private int invoiceId;
    private int reservationId;
    private Reservation reservation;
    private LocalDate invoiceDate;
    private double roomCharges;
    private double additionalCharges;
    private double taxAmount;
    private double totalAmount;
    private PaymentStatus paymentStatus;
    private String paymentMethod;
    private LocalDate paymentDate;
    private String notes;
    private List<InvoiceItem> items;
    private String createdAt;
    private String updatedAt;

    // Constructor for creating new invoice
    public Invoice(Reservation reservation, LocalDate invoiceDate, double roomCharges,
                  double additionalCharges, double taxAmount, PaymentStatus paymentStatus) {
        this.reservation = reservation;
        this.reservationId = reservation.getReservationId();
        this.invoiceDate = invoiceDate;
        this.roomCharges = roomCharges;
        this.additionalCharges = additionalCharges;
        this.taxAmount = taxAmount;
        this.totalAmount = roomCharges + additionalCharges + taxAmount;
        this.paymentStatus = paymentStatus;
    }
} 