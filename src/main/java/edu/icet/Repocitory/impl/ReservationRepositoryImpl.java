package edu.icet.Repocitory.impl;

import edu.icet.Db.DBConnection;
import edu.icet.Model.Reservation;
import edu.icet.Model.Room;
import edu.icet.Utill.RoomType;
import edu.icet.Repocitory.ReservationRepository;
import edu.icet.Utill.PaymentStatus;
import edu.icet.Utill.ReservationStatus;
import edu.icet.Utill.HibernateUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class ReservationRepositoryImpl implements ReservationRepository {

    @Override
    public boolean save(Reservation reservation) {
        Connection connection = null;
        PreparedStatement stmt = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "INSERT INTO reservations (customer_id, room_id, check_in_date, check_out_date, " +
                        "num_guests, total_price, status, payment_status, special_requests) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = connection.prepareStatement(sql);
            
            stmt.setInt(1, reservation.getCustomer().getCustomerId());
            System.out.println("Customer ID: " + reservation.getCustomer().getCustomerId());
            stmt.setInt(2, reservation.getRoom().getRoomId());
            System.out.println("Room ID: " + reservation.getRoom().getRoomId());
            stmt.setDate(3, Date.valueOf(reservation.getCheckInDate()));
            System.out.println("Check-in Date: " + reservation.getCheckInDate());
            stmt.setDate(4, Date.valueOf(reservation.getCheckOutDate()));
            System.out.println("Check-out Date: " + reservation.getCheckOutDate());
            stmt.setInt(5, reservation.getNumGuests());
            System.out.println("Number of Guests: " + reservation.getNumGuests());
            stmt.setDouble(6, reservation.getTotalPrice());
            System.out.println("Total Price: " + reservation.getTotalPrice());
            stmt.setString(7, reservation.getStatus().toString());
            System.out.println("Status: " + reservation.getStatus().toString());
            stmt.setString(8, reservation.getPaymentStatus().toString());
            System.out.println("Payment Status: " + reservation.getPaymentStatus().toString());
            stmt.setString(9, reservation.getSpecialRequests());
            System.out.println("Special Requests: " + reservation.getSpecialRequests());
            
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
    public boolean update(Reservation reservation) {
        Connection connection = null;
        PreparedStatement stmt = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "UPDATE reservations SET customer_id=?, room_id=?, check_in_date=?, check_out_date=?, " +
                        "num_guests=?, total_price=?, status=?, payment_status=?, special_requests=? " +
                        "WHERE reservation_id=?";
            stmt = connection.prepareStatement(sql);
            
            stmt.setInt(1, reservation.getCustomerId());
            stmt.setInt(2, reservation.getRoomId());
            stmt.setDate(3, Date.valueOf(reservation.getCheckInDate()));
            stmt.setDate(4, Date.valueOf(reservation.getCheckOutDate()));
            stmt.setInt(5, reservation.getNumGuests());
            stmt.setDouble(6, reservation.getTotalPrice());
            stmt.setString(7, reservation.getStatus().toString());
            stmt.setString(8, reservation.getPaymentStatus().toString());
            stmt.setString(9, reservation.getSpecialRequests());
            stmt.setInt(10, reservation.getReservationId());
            
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
            String sql = "DELETE FROM reservations WHERE reservation_id=?";
            stmt = connection.prepareStatement(sql);
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
    public Reservation findById(int id) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "SELECT r.*, c.name as customer_name, rm.room_number, rm.room_type, rm.price_per_night " +
                        "FROM reservations r " +
                        "JOIN customers c ON r.customer_id = c.customer_id " +
                        "JOIN rooms rm ON r.room_id = rm.room_id " +
                        "WHERE r.reservation_id=?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractReservationFromResultSet(rs);
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
    public List<Reservation> findAll() {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Reservation> reservations = new ArrayList<>();
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "SELECT r.*, c.name as customer_name, rm.room_number, rm.room_type, rm.price_per_night " +
                        "FROM reservations r " +
                        "JOIN customers c ON r.customer_id = c.customer_id " +
                        "JOIN rooms rm ON r.room_id = rm.room_id " +
                        "ORDER BY r.check_in_date DESC";
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                reservations.add(extractReservationFromResultSet(rs));
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
        
        return reservations;
    }

    @Override
    public List<Reservation> findByCustomerId(int customerId) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Reservation> reservations = new ArrayList<>();
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "SELECT r.*, c.name as customer_name, rm.room_number, rm.room_type, rm.price_per_night " +
                        "FROM reservations r " +
                        "JOIN customers c ON r.customer_id = c.customer_id " +
                        "JOIN rooms rm ON r.room_id = rm.room_id " +
                        "WHERE r.customer_id=? " +
                        "ORDER BY r.check_in_date DESC";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, customerId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                reservations.add(extractReservationFromResultSet(rs));
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
        
        return reservations;
    }

    @Override
    public List<Reservation> findByStatus(ReservationStatus status) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Reservation> reservations = new ArrayList<>();
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "SELECT r.*, c.name as customer_name, rm.room_number, rm.room_type, rm.price_per_night " +
                        "FROM reservations r " +
                        "JOIN customers c ON r.customer_id = c.customer_id " +
                        "JOIN rooms rm ON r.room_id = rm.room_id " +
                        "WHERE r.status=? " +
                        "ORDER BY r.check_in_date DESC";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, status.toString());
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                reservations.add(extractReservationFromResultSet(rs));
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
        
        return reservations;
    }

    @Override
    public List<Reservation> findByDateRange(LocalDate startDate, LocalDate endDate) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Reservation> reservations = new ArrayList<>();
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "SELECT r.*, c.name as customer_name, rm.room_number, rm.room_type, rm.price_per_night " +
                        "FROM reservations r " +
                        "JOIN customers c ON r.customer_id = c.customer_id " +
                        "JOIN rooms rm ON r.room_id = rm.room_id " +
                        "WHERE (r.check_in_date BETWEEN ? AND ?) OR (r.check_out_date BETWEEN ? AND ?) " +
                        "ORDER BY r.check_in_date";
            stmt = connection.prepareStatement(sql);
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            stmt.setDate(3, Date.valueOf(startDate));
            stmt.setDate(4, Date.valueOf(endDate));
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                reservations.add(extractReservationFromResultSet(rs));
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
        
        return reservations;
    }

    @Override
    public boolean isRoomAvailable(int roomId, LocalDate checkIn, LocalDate checkOut) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "SELECT COUNT(*) FROM reservations " +
                        "WHERE room_id = ? AND status != 'Cancelled' AND " +
                        "((check_in_date BETWEEN ? AND ?) OR " +
                        "(check_out_date BETWEEN ? AND ?) OR " +
                        "(check_in_date <= ? AND check_out_date >= ?))";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, roomId);
            stmt.setDate(2, Date.valueOf(checkIn));
            stmt.setDate(3, Date.valueOf(checkOut));
            stmt.setDate(4, Date.valueOf(checkIn));
            stmt.setDate(5, Date.valueOf(checkOut));
            stmt.setDate(6, Date.valueOf(checkIn));
            stmt.setDate(7, Date.valueOf(checkOut));
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
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
        
        return false;
    }

    @Override
    public List<Reservation> searchReservations(String query) {
        String searchQuery = "SELECT r FROM Reservation r " +
                            "WHERE LOWER(r.customerName) LIKE LOWER(:query) " +
                            "OR LOWER(r.roomNumber) LIKE LOWER(:query)";
        
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query<Reservation> hibernateQuery = session.createQuery(searchQuery, Reservation.class);
            hibernateQuery.setParameter("query", "%" + query + "%");
            return hibernateQuery.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private Reservation extractReservationFromResultSet(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setReservationId(rs.getInt("reservation_id"));
        reservation.setCustomerId(rs.getInt("customer_id"));
        reservation.setRoomId(rs.getInt("room_id"));
        reservation.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        reservation.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        reservation.setNumGuests(rs.getInt("num_guests"));
        reservation.setTotalPrice(rs.getDouble("total_price"));
        reservation.setStatus(ReservationStatus.valueOf(rs.getString("status")));
        reservation.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));
        reservation.setSpecialRequests(rs.getString("special_requests"));
        reservation.setCreatedAt(rs.getString("created_at"));
        reservation.setUpdatedAt(rs.getString("updated_at"));
        
        // Additional display fields
        reservation.setCustomerName(rs.getString("customer_name"));
        reservation.setRoomNumber(rs.getString("room_number"));
        
        // Set room type and price
        Room room = new Room();
        room.setRoomId(rs.getInt("room_id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setRoomType(RoomType.valueOf(rs.getString("room_type")));
        room.setPricePerNight(rs.getDouble("price_per_night"));
        reservation.setRoom(room);
        
        return reservation;
    }
} 