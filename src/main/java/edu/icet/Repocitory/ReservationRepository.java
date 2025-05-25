package edu.icet.Repocitory;

import edu.icet.Model.Reservation;
import edu.icet.Utill.ReservationStatus;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository {
    boolean save(Reservation reservation);
    boolean update(Reservation reservation);
    boolean delete(int id);
    Reservation findById(int id);
    List<Reservation> findAll();
    List<Reservation> findByCustomerId(int customerId);
    List<Reservation> findByStatus(ReservationStatus status);
    List<Reservation> findByDateRange(LocalDate startDate, LocalDate endDate);
    boolean isRoomAvailable(int roomId, LocalDate checkIn, LocalDate checkOut);
    List<Reservation> searchReservations(String query);
} 