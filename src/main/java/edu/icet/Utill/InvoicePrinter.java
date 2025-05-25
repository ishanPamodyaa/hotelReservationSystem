package edu.icet.Utill;

import edu.icet.Model.Invoice;
import edu.icet.Model.InvoiceItem;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import java.time.format.DateTimeFormatter;

public class InvoicePrinter {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void printInvoice(Invoice invoice) {
        VBox content = createInvoiceContent(invoice);
        
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            boolean proceed = job.showPrintDialog(null);
            if (proceed) {
                boolean printed = job.printPage(content);
                if (printed) {
                    job.endJob();
                }
            }
        }
    }

    private static VBox createInvoiceContent(Invoice invoice) {
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20;");

        // Header
        Text title = new Text("HOTEL INVOICE");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        content.getChildren().add(title);

        // Invoice details
        addSection(content, "Invoice Details");
        addField(content, "Invoice ID", String.valueOf(invoice.getInvoiceId()));
        addField(content, "Date", invoice.getInvoiceDate().format(DATE_FORMATTER));
        addField(content, "Payment Method", invoice.getPaymentMethod());
        addField(content, "Payment Status", invoice.getPaymentStatus().toString());

        // Customer details
        addSection(content, "Customer Details");
        addField(content, "Customer Name", invoice.getReservation().getCustomerName());
        addField(content, "Room Number", invoice.getReservation().getRoomNumber());

        // Items
        addSection(content, "Invoice Items");
        for (InvoiceItem item : invoice.getItems()) {
            addItemRow(content, item);
        }

        // Charges
        addSection(content, "Charges");
        addField(content, "Room Charges", String.format("$%.2f", invoice.getRoomCharges()));
        addField(content, "Additional Charges", String.format("$%.2f", invoice.getAdditionalCharges()));
        addField(content, "Tax Amount", String.format("$%.2f", invoice.getTaxAmount()));
        
        // Total
        Text totalLabel = new Text("Total Amount");
        totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Text totalAmount = new Text(String.format("$%.2f", invoice.getTotalAmount()));
        totalAmount.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        content.getChildren().addAll(
            createSeparator(),
            totalLabel,
            totalAmount
        );

        // Notes
        if (invoice.getNotes() != null && !invoice.getNotes().isEmpty()) {
            addSection(content, "Notes");
            Label notes = new Label(invoice.getNotes());
            notes.setWrapText(true);
            content.getChildren().add(notes);
        }

        return content;
    }

    private static void addSection(VBox container, String title) {
        container.getChildren().addAll(
            createSeparator(),
            createSectionTitle(title)
        );
    }

    private static void addField(VBox container, String label, String value) {
        Label fieldLabel = new Label(label + ": " + value);
        fieldLabel.setFont(Font.font("Arial", 12));
        container.getChildren().add(fieldLabel);
    }

    private static void addItemRow(VBox container, InvoiceItem item) {
        Label itemLabel = new Label(String.format(
            "%s - %d x $%.2f = $%.2f",
            item.getDescription(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.getAmount()
        ));
        itemLabel.setFont(Font.font("Arial", 12));
        container.getChildren().add(itemLabel);
    }

    private static Node createSeparator() {
        Label separator = new Label();
        separator.setPrefHeight(20);
        return separator;
    }

    private static Text createSectionTitle(String title) {
        Text sectionTitle = new Text(title);
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        return sectionTitle;
    }
} 