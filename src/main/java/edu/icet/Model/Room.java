package edu.icet.Model;

import edu.icet.Utill.RoomStatus;
import edu.icet.Utill.RoomType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    private int roomId;
    private String roomNumber;
    private RoomType roomType;
    private double pricePerNight;
    private RoomStatus status;
    private int capacity;
    private String description;

    // Constructor for table view
    public Room(int roomId, String roomNumber, RoomType roomType, double pricePerNight, RoomStatus status) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.status = status;
    }
} 