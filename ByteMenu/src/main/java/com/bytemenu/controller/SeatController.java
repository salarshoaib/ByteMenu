package com.bytemenu.controller;

import com.bytemenu.dao.SeatReservationDAO;
import com.bytemenu.model.SeatReservation;
import com.bytemenu.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;

public class SeatController {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> slotPicker;
    @FXML private VBox seatArea, myReservationsList;
    @FXML private Label summaryDate, summarySlot, summarySeat, reserveMsg;
    @FXML private Button btnReserve;

    private final SeatReservationDAO dao = new SeatReservationDAO();

    // 5 tables x 6 seats = 30 total seats
    private static final int TABLES = 5;
    private static final int SEATS_PER_TABLE = 6;

    private String selectedSeat = null;

    @FXML
    public void initialize() {
        slotPicker.getItems().addAll(
                "08:00 – 09:30", "09:30 – 11:00", "11:00 – 12:30",
                "12:30 – 14:00", "14:00 – 15:30", "15:30 – 17:00"
        );
        datePicker.setValue(LocalDate.now());
        slotPicker.getSelectionModel().selectFirst();
        loadSeats();
        loadMyReservations();
    }

    @FXML
    void loadSeats() {
        seatArea.getChildren().clear();
        selectedSeat = null;
        updateSummary();

        String date = datePicker.getValue() != null ? datePicker.getValue().toString() : "";
        String slot = slotPicker.getValue() != null ? slotPicker.getValue() : "";

        summaryDate.setText(date.isEmpty() ? "—" : date);
        summarySlot.setText(slot.isEmpty() ? "—" : slot);

        List<String> booked;
        try { booked = dao.getBookedSeats(date, slot); }
        catch (Exception e) { booked = List.of(); }

        for (int t = 1; t <= TABLES; t++) {
            VBox tableBox = new VBox(6);
            tableBox.getStyleClass().add("card");
            tableBox.setStyle("-fx-padding:12 16 12 16;-fx-background-radius:6;");

            Label tableLabel = new Label("Table " + t);
            tableLabel.setStyle("-fx-font-weight:bold;-fx-font-size:12px;-fx-text-fill:#555;");
            tableBox.getChildren().add(tableLabel);

            HBox seats = new HBox(8);
            for (int s = 1; s <= SEATS_PER_TABLE; s++) {
                String seatId = "T" + t + "-S" + s;
                Button seatBtn = new Button(seatId);

                if (booked.contains(seatId)) {
                    seatBtn.getStyleClass().add("seat-booked");
                    seatBtn.setDisable(true);
                } else {
                    seatBtn.getStyleClass().add("seat-available");
                    final String sid = seatId;
                    seatBtn.setOnAction(e -> selectSeat(sid, seatBtn));
                }
                seats.getChildren().add(seatBtn);
            }
            tableBox.getChildren().add(seats);
            seatArea.getChildren().add(tableBox);
        }
    }

    private void selectSeat(String seatId, Button seatBtn) {
        // Reset all seat buttons to available style
        seatArea.getChildren().forEach(tableNode -> {
            if (tableNode instanceof VBox tb) {
                tb.getChildren().forEach(child -> {
                    if (child instanceof HBox hb) {
                        hb.getChildren().forEach(btn -> {
                            if (btn instanceof Button b && !b.isDisabled()) {
                                b.getStyleClass().setAll("seat-available");
                            }
                        });
                    }
                });
            }
        });
        seatBtn.getStyleClass().setAll("seat-selected");
        selectedSeat = seatId;
        updateSummary();
    }

    private void updateSummary() {
        summarySeat.setText(selectedSeat != null ? selectedSeat : "None selected");
        btnReserve.setDisable(selectedSeat == null);
        reserveMsg.setText("");
    }

    @FXML
    void handleReserve() {
        if (selectedSeat == null) return;
        String date = datePicker.getValue().toString();
        String slot = slotPicker.getValue();

        SeatReservation r = new SeatReservation();
        r.setUserId(SessionManager.getCurrentUser().getId());
        r.setSeatNumber(selectedSeat);
        r.setDate(date);
        r.setTimeSlot(slot);

        try {
            dao.reserve(r);
            reserveMsg.setStyle("-fx-text-fill:#0A5233;-fx-font-size:12px;-fx-font-weight:bold;");
            reserveMsg.setText("✓ Seat " + selectedSeat + " reserved for " + slot + " on " + date);
            selectedSeat = null;
            loadSeats();
            loadMyReservations();
        } catch (Exception e) {
            reserveMsg.setStyle("-fx-text-fill:#C8372D;-fx-font-size:12px;");
            reserveMsg.setText(e.getMessage());
        }
    }

    private void loadMyReservations() {
        myReservationsList.getChildren().clear();
        try {
            List<SeatReservation> list = dao.getByUser(SessionManager.getCurrentUser().getId());
            if (list.isEmpty()) {
                Label none = new Label("No upcoming reservations");
                none.setStyle("-fx-text-fill:#AAAAAA;-fx-font-size:12px;");
                myReservationsList.getChildren().add(none);
                return;
            }
            for (SeatReservation r : list) {
                VBox card = new VBox(2);
                card.setStyle("-fx-background-color:#EBF3FB;-fx-background-radius:4;-fx-padding:8 10 8 10;");
                Label seatLbl = new Label("Seat " + r.getSeatNumber());
                seatLbl.setStyle("-fx-font-weight:bold;-fx-font-size:12px;-fx-text-fill:#1F4E79;");
                Label info = new Label(r.getDate() + "  ·  " + r.getTimeSlot());
                info.setStyle("-fx-font-size:11px;-fx-text-fill:#555;");
                card.getChildren().addAll(seatLbl, info);
                myReservationsList.getChildren().add(card);
            }
        } catch (Exception e) {
            myReservationsList.getChildren().add(new Label("Could not load reservations."));
        }
    }
}
