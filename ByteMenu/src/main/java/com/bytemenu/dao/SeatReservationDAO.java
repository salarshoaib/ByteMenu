package com.bytemenu.dao;

import com.bytemenu.model.SeatReservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeatReservationDAO {

    private final Connection conn = DatabaseManager.getInstance().getConnection();

    public SeatReservation reserve(SeatReservation r) throws Exception {
        // Check if seat already booked for that date+slot
        String check = "SELECT id FROM seat_reservations WHERE seat_number=? AND date=? AND time_slot=? AND status='Confirmed'";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, r.getSeatNumber());
            ps.setString(2, r.getDate());
            ps.setString(3, r.getTimeSlot());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) throw new Exception("Seat " + r.getSeatNumber() + " is already booked for that slot.");
        }

        String sql = "INSERT INTO seat_reservations (user_id, seat_number, date, time_slot, status) VALUES (?,?,?,?,'Confirmed')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getUserId());
            ps.setString(2, r.getSeatNumber());
            ps.setString(3, r.getDate());
            ps.setString(4, r.getTimeSlot());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) r.setId(keys.getInt(1));
        }
        return r;
    }

    public List<SeatReservation> getByUser(int userId) throws Exception {
        String sql = "SELECT * FROM seat_reservations WHERE user_id=? ORDER BY date, time_slot";
        List<SeatReservation> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    /** Returns list of already-booked seat numbers for a date+slot combo */
    public List<String> getBookedSeats(String date, String timeSlot) throws Exception {
        String sql = "SELECT seat_number FROM seat_reservations WHERE date=? AND time_slot=? AND status='Confirmed'";
        List<String> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date);
            ps.setString(2, timeSlot);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rs.getString("seat_number"));
        }
        return list;
    }

    private SeatReservation map(ResultSet rs) throws SQLException {
        return new SeatReservation(rs.getInt("id"), rs.getInt("user_id"), rs.getString("seat_number"),
                rs.getString("date"), rs.getString("time_slot"), rs.getString("status"));
    }
}
