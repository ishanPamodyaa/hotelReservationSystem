package edu.icet.Repocitory.impl;

import edu.icet.Db.DBConnection;
import edu.icet.Model.Room;
import edu.icet.Utill.RoomType;
import edu.icet.Repocitory.RoomRepository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoomRepositoryImpl implements RoomRepository {
    private final Connection connection;

    public RoomRepositoryImpl() {
        connection = DBConnection.getInstance().getConnection();
    }

    @Override
    public boolean save(Room room) {
        String sql = "INSERT INTO rooms (room_number, room_type, price_per_night, capacity, description) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, room.getRoomNumber());
            stmt.setString(2, room.getRoomType().toString());
            stmt.setDouble(3, room.getPricePerNight());
            stmt.setInt(4, room.getCapacity());
            stmt.setString(5, room.getDescription());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Room room) {
        String sql = "UPDATE rooms SET room_number=?, room_type=?, price_per_night=?, capacity=?, description=? WHERE room_id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, room.getRoomNumber());
            stmt.setString(2, room.getRoomType().toString());
            stmt.setDouble(3, room.getPricePerNight());
            stmt.setInt(4, room.getCapacity());
            stmt.setString(5, room.getDescription());
            stmt.setInt(6, room.getRoomId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int roomId) {
        String sql = "DELETE FROM rooms WHERE room_id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Room findById(int roomId) {
        String sql = "SELECT * FROM rooms WHERE room_id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractRoomFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Room> findAll() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY room_number";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rooms.add(extractRoomFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    @Override
    public List<Room> searchRooms(String searchTerm) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE room_number LIKE ? OR room_type LIKE ? OR description LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rooms.add(extractRoomFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    @Override
    public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT DISTINCT r.* FROM rooms r " +
                    "WHERE r.room_id NOT IN ( " +
                    "    SELECT res.room_id FROM reservations res " +
                    "    WHERE (? BETWEEN res.check_in_date AND res.check_out_date " +
                    "    OR ? BETWEEN res.check_in_date AND res.check_out_date " +
                    "    OR (res.check_in_date BETWEEN ? AND ?)) " +
                    "    AND res.status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
                    ") " +
                    "ORDER BY r.room_number";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(checkIn));
            stmt.setDate(2, Date.valueOf(checkOut));
            stmt.setDate(3, Date.valueOf(checkIn));
            stmt.setDate(4, Date.valueOf(checkOut));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rooms.add(extractRoomFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    @Override
    public List<Room> findByType(RoomType type) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Room> rooms = new ArrayList<>();
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "SELECT * FROM rooms WHERE room_type = ? ORDER BY room_number";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, type.toString());
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                rooms.add(extractRoomFromResultSet(rs));
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
        
        return rooms;
    }

    private Room extractRoomFromResultSet(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setRoomId(rs.getInt("room_id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setRoomType(RoomType.valueOf(rs.getString("room_type")));
        //room.setRoomType(rs.getString("room_type"));
        room.setPricePerNight(rs.getDouble("price_per_night"));
        room.setCapacity(rs.getInt("capacity"));
        room.setDescription(rs.getString("description"));
        return room;
    }
} 