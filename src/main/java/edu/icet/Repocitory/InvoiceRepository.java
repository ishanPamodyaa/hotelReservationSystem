package edu.icet.Repocitory;

import edu.icet.Model.Invoice;
import edu.icet.Model.InvoiceItem;
import edu.icet.Utill.PaymentStatus;
import java.time.LocalDate;
import java.util.List;

public interface InvoiceRepository {
    boolean save(Invoice invoice);
    boolean update(Invoice invoice);
    boolean delete(int id);
    Invoice findById(int id);
    List<Invoice> findAll();
    List<Invoice> findByReservationId(int reservationId);
    List<Invoice> findByDateRange(LocalDate startDate, LocalDate endDate);
    List<Invoice> findByPaymentStatus(PaymentStatus status);
    boolean addInvoiceItem(InvoiceItem item);
    boolean updateInvoiceItem(InvoiceItem item);
    boolean deleteInvoiceItem(int itemId);
    List<InvoiceItem> findItemsByInvoiceId(int invoiceId);
    double calculateTotalAmount(int invoiceId);
} 