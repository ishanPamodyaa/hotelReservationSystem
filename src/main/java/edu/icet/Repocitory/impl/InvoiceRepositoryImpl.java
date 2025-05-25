package edu.icet.Repocitory.impl;

import edu.icet.Db.DBConnection;
import edu.icet.Model.Invoice;
import edu.icet.Model.InvoiceItem;
import edu.icet.Model.Reservation;
import edu.icet.Repocitory.InvoiceRepository;
import edu.icet.Utill.PaymentStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InvoiceRepositoryImpl implements InvoiceRepository {

    @Override
    public boolean save(Invoice invoice) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "INSERT INTO invoices (reservation_id, invoice_date, room_charges, " +
                        "additional_charges, tax_amount, total_amount, payment_status, payment_method, " +
                        "payment_date, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setInt(1, invoice.getReservationId());
            stmt.setDate(2, Date.valueOf(invoice.getInvoiceDate()));
            stmt.setDouble(3, invoice.getRoomCharges());
            stmt.setDouble(4, invoice.getAdditionalCharges());
            stmt.setDouble(5, invoice.getTaxAmount());
            stmt.setDouble(6, invoice.getTotalAmount());
            stmt.setString(7, invoice.getPaymentStatus().toString());
            stmt.setString(8, invoice.getPaymentMethod());
            stmt.setDate(9, invoice.getPaymentDate() != null ? Date.valueOf(invoice.getPaymentDate()) : null);
            stmt.setString(10, invoice.getNotes());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    invoice.setInvoiceId(generatedKeys.getInt(1));
                    // Save invoice items if any
                    if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
                        for (InvoiceItem item : invoice.getItems()) {
                            item.setInvoiceId(invoice.getInvoiceId());
                            addInvoiceItem(item);
                        }
                    }
                    return true;
                }
            }
            return false;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean update(Invoice invoice) {
        Connection connection = null;
        PreparedStatement stmt = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "UPDATE invoices SET room_charges=?, additional_charges=?, tax_amount=?, " +
                        "total_amount=?, payment_status=?, payment_method=?, payment_date=?, notes=? " +
                        "WHERE invoice_id=?";
            stmt = connection.prepareStatement(sql);
            
            stmt.setDouble(1, invoice.getRoomCharges());
            stmt.setDouble(2, invoice.getAdditionalCharges());
            stmt.setDouble(3, invoice.getTaxAmount());
            stmt.setDouble(4, invoice.getTotalAmount());
            stmt.setString(5, invoice.getPaymentStatus().toString());
            stmt.setString(6, invoice.getPaymentMethod());
            stmt.setDate(7, invoice.getPaymentDate() != null ? Date.valueOf(invoice.getPaymentDate()) : null);
            stmt.setString(8, invoice.getNotes());
            stmt.setInt(9, invoice.getInvoiceId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean delete(int id) {
        Connection connection = null;
        PreparedStatement stmt = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            // First delete all invoice items
            String deleteItems = "DELETE FROM invoice_items WHERE invoice_id=?";
            stmt = connection.prepareStatement(deleteItems);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            
            // Then delete the invoice
            String deleteInvoice = "DELETE FROM invoices WHERE invoice_id=?";
            stmt = connection.prepareStatement(deleteInvoice);
            stmt.setInt(1, id);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Invoice findById(int id) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "SELECT i.*, r.check_in_date, r.check_out_date, r.total_price as reservation_total, " +
                        "c.name as customer_name, rm.room_number " +
                        "FROM invoices i " +
                        "JOIN reservations r ON i.reservation_id = r.reservation_id " +
                        "JOIN customers c ON r.customer_id = c.customer_id " +
                        "JOIN rooms rm ON r.room_id = rm.room_id " +
                        "WHERE i.invoice_id=?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                Invoice invoice = extractInvoiceFromResultSet(rs);
                invoice.setItems(findItemsByInvoiceId(id));
                return invoice;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return null;
    }

    @Override
    public List<Invoice> findAll() {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Invoice> invoices = new ArrayList<>();
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "SELECT i.*, r.check_in_date, r.check_out_date, r.total_price as reservation_total, " +
                        "c.name as customer_name, rm.room_number " +
                        "FROM invoices i " +
                        "JOIN reservations r ON i.reservation_id = r.reservation_id " +
                        "JOIN customers c ON r.customer_id = c.customer_id " +
                        "JOIN rooms rm ON r.room_id = rm.room_id " +
                        "ORDER BY i.invoice_date DESC";
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Invoice invoice = extractInvoiceFromResultSet(rs);
                invoice.setItems(findItemsByInvoiceId(invoice.getInvoiceId()));
                invoices.add(invoice);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return invoices;
    }

    @Override
    public List<Invoice> findByReservationId(int reservationId) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Invoice> invoices = new ArrayList<>();
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "SELECT i.*, r.check_in_date, r.check_out_date, r.total_price as reservation_total, " +
                        "c.name as customer_name, rm.room_number " +
                        "FROM invoices i " +
                        "JOIN reservations r ON i.reservation_id = r.reservation_id " +
                        "JOIN customers c ON r.customer_id = c.customer_id " +
                        "JOIN rooms rm ON r.room_id = rm.room_id " +
                        "WHERE i.reservation_id=? " +
                        "ORDER BY i.invoice_date DESC";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, reservationId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Invoice invoice = extractInvoiceFromResultSet(rs);
                invoice.setItems(findItemsByInvoiceId(invoice.getInvoiceId()));
                invoices.add(invoice);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return invoices;
    }

    @Override
    public List<Invoice> findByDateRange(LocalDate startDate, LocalDate endDate) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Invoice> invoices = new ArrayList<>();
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "SELECT i.*, r.check_in_date, r.check_out_date, r.total_price as reservation_total, " +
                        "c.name as customer_name, rm.room_number " +
                        "FROM invoices i " +
                        "JOIN reservations r ON i.reservation_id = r.reservation_id " +
                        "JOIN customers c ON r.customer_id = c.customer_id " +
                        "JOIN rooms rm ON r.room_id = rm.room_id " +
                        "WHERE i.invoice_date BETWEEN ? AND ? " +
                        "ORDER BY i.invoice_date DESC";
            stmt = connection.prepareStatement(sql);
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Invoice invoice = extractInvoiceFromResultSet(rs);
                invoice.setItems(findItemsByInvoiceId(invoice.getInvoiceId()));
                invoices.add(invoice);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return invoices;
    }

    @Override
    public List<Invoice> findByPaymentStatus(PaymentStatus status) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Invoice> invoices = new ArrayList<>();
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "SELECT i.*, r.check_in_date, r.check_out_date, r.total_price as reservation_total, " +
                        "c.name as customer_name, rm.room_number " +
                        "FROM invoices i " +
                        "JOIN reservations r ON i.reservation_id = r.reservation_id " +
                        "JOIN customers c ON r.customer_id = c.customer_id " +
                        "JOIN rooms rm ON r.room_id = rm.room_id " +
                        "WHERE i.payment_status=? " +
                        "ORDER BY i.invoice_date DESC";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, status.toString());
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Invoice invoice = extractInvoiceFromResultSet(rs);
                invoice.setItems(findItemsByInvoiceId(invoice.getInvoiceId()));
                invoices.add(invoice);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return invoices;
    }

    @Override
    public boolean addInvoiceItem(InvoiceItem item) {
        Connection connection = null;
        PreparedStatement stmt = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "INSERT INTO invoice_items (invoice_id, description, quantity, unit_price, amount) " +
                        "VALUES (?, ?, ?, ?, ?)";
            stmt = connection.prepareStatement(sql);
            
            stmt.setInt(1, item.getInvoiceId());
            stmt.setString(2, item.getDescription());
            stmt.setInt(3, item.getQuantity());
            stmt.setDouble(4, item.getUnitPrice());
            stmt.setDouble(5, item.getAmount());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean updateInvoiceItem(InvoiceItem item) {
        Connection connection = null;
        PreparedStatement stmt = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "UPDATE invoice_items SET description=?, quantity=?, unit_price=?, amount=? " +
                        "WHERE item_id=?";
            stmt = connection.prepareStatement(sql);
            
            stmt.setString(1, item.getDescription());
            stmt.setInt(2, item.getQuantity());
            stmt.setDouble(3, item.getUnitPrice());
            stmt.setDouble(4, item.getAmount());
            stmt.setInt(5, item.getItemId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean deleteInvoiceItem(int itemId) {
        Connection connection = null;
        PreparedStatement stmt = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "DELETE FROM invoice_items WHERE item_id=?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, itemId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<InvoiceItem> findItemsByInvoiceId(int invoiceId) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<InvoiceItem> items = new ArrayList<>();
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "SELECT * FROM invoice_items WHERE invoice_id=?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, invoiceId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                items.add(extractInvoiceItemFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return items;
    }

    @Override
    public double calculateTotalAmount(int invoiceId) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "SELECT SUM(amount) FROM invoice_items WHERE invoice_id=?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, invoiceId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble(1);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return 0.0;
    }

    private Invoice extractInvoiceFromResultSet(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(rs.getInt("invoice_id"));
        invoice.setReservationId(rs.getInt("reservation_id"));
        invoice.setInvoiceDate(rs.getDate("invoice_date").toLocalDate());
        invoice.setRoomCharges(rs.getDouble("room_charges"));
        invoice.setAdditionalCharges(rs.getDouble("additional_charges"));
        invoice.setTaxAmount(rs.getDouble("tax_amount"));
        invoice.setTotalAmount(rs.getDouble("total_amount"));
        invoice.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));
        invoice.setPaymentMethod(rs.getString("payment_method"));
        Date paymentDate = rs.getDate("payment_date");
        if (paymentDate != null) {
            invoice.setPaymentDate(paymentDate.toLocalDate());
        }
        invoice.setNotes(rs.getString("notes"));
        invoice.setCreatedAt(rs.getString("created_at"));
        invoice.setUpdatedAt(rs.getString("updated_at"));
        
        // Set reservation details
        Reservation reservation = new Reservation();
        reservation.setReservationId(rs.getInt("reservation_id"));
        reservation.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        reservation.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        reservation.setTotalPrice(rs.getDouble("reservation_total"));
        reservation.setCustomerName(rs.getString("customer_name"));
        reservation.setRoomNumber(rs.getString("room_number"));
        invoice.setReservation(reservation);
        
        return invoice;
    }

    private InvoiceItem extractInvoiceItemFromResultSet(ResultSet rs) throws SQLException {
        InvoiceItem item = new InvoiceItem();
        item.setItemId(rs.getInt("item_id"));
        item.setInvoiceId(rs.getInt("invoice_id"));
        item.setDescription(rs.getString("description"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getDouble("unit_price"));
        item.setAmount(rs.getDouble("amount"));
        item.setCreatedAt(rs.getString("created_at"));
        item.setUpdatedAt(rs.getString("updated_at"));
        return item;
    }
} 