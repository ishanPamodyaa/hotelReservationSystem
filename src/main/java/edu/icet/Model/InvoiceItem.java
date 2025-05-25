package edu.icet.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItem {
    private int itemId;
    private int invoiceId;
    private String description;
    private int quantity;
    private double unitPrice;
    private double amount;
    private String createdAt;
    private String updatedAt;

    // Constructor for creating new invoice item
    public InvoiceItem(String description, int quantity, double unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = quantity * unitPrice;
    }
} 