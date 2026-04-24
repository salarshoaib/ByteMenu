package com.bytemenu.model;

public class SeatReservation {
    private int id;
    private int userId;
    private String seatNumber;
    private String date;
    private String timeSlot;
    private String status;

    public SeatReservation() {}

    public SeatReservation(int id, int userId, String seatNumber, String date, String timeSlot, String status) {
        this.id = id; this.userId = userId; this.seatNumber = seatNumber;
        this.date = date; this.timeSlot = timeSlot; this.status = status;
    }

    public int getId()            { return id; }
    public void setId(int id)     { this.id = id; }
    public int getUserId()        { return userId; }
    public void setUserId(int u)  { this.userId = u; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String s){ this.seatNumber = s; }
    public String getDate()       { return date; }
    public void setDate(String d) { this.date = d; }
    public String getTimeSlot()   { return timeSlot; }
    public void setTimeSlot(String t){ this.timeSlot = t; }
    public String getStatus()     { return status; }
    public void setStatus(String s){ this.status = s; }
}
