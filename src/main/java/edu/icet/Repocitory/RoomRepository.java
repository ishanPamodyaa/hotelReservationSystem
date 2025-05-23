package edu.icet.Repocitory;

import edu.icet.Model.Room;
import edu.icet.Model.RoomType;
import java.util.List;

public interface RoomRepository {
    boolean save(Room room);
    boolean update(Room room);
    boolean delete(int roomId);
    Room findById(int roomId);
    List<Room> findAll();
    List<Room> findByType(RoomType type);
    List<Room> searchRooms(String searchTerm);
    List<Room> findAvailableRooms(java.time.LocalDate checkIn, java.time.LocalDate checkOut);
} 