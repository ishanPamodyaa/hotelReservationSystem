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
    private Integer id;
    private String roomNumber;
    private RoomType type;
    private Double price;
    private RoomStatus status;
} 