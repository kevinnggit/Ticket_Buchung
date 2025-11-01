package hbv.web;

import hbv.web.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;


public class DeleteT extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            PrintWriter out = response.getWriter();
            out.println("{\"status\":\"error\",\"message\":\"Nicht angemeldet.\"}");
            return;
        }

        // Benutzer-ID aus der Session abrufen
        Integer userId = (Integer) session.getAttribute("user_id");
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            PrintWriter out = response.getWriter();
            out.println("{\"status\":\"error\",\"message\":\"Benutzer-ID nicht gefunden.\"}");
            return;
        }

        // Lese Termin-ID aus der Anfrage (Parameter "id")
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = response.getWriter();
            out.println("{\"status\":\"error\",\"message\":\"Keine Termin-ID angegeben.\"}");
            return;
        }

        int appointmentId;
        try {
            appointmentId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = response.getWriter();
            out.println("{\"status\":\"error\",\"message\":\"Ungültige Termin-ID.\"}");
            return;
        }

        // Lese center_id, date1, time_slot_id, vaccine_id
        String centerIdStr = request.getParameter("center_id");
        String date1 = request.getParameter("date1");
        String timeSlotIdStr = request.getParameter("time_slot_id");
        String vaccineIdStr = request.getParameter("vaccine_id");

        if (centerIdStr == null || centerIdStr.isEmpty() ||
                date1 == null || date1.isEmpty() ||
                timeSlotIdStr == null || timeSlotIdStr.isEmpty() ||
                vaccineIdStr == null || vaccineIdStr.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = response.getWriter();
            out.println("{\"status\":\"error\",\"message\":\"Erforderliche Parameter fehlen.\"}");
            return;
        }

        int centerId, timeSlotId, vaccineId;
        try {
            centerId = Integer.parseInt(centerIdStr);
            timeSlotId = Integer.parseInt(timeSlotIdStr);
            vaccineId = Integer.parseInt(vaccineIdStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = response.getWriter();
            out.println("{\"status\":\"error\",\"message\":\"Ungültige Parameterwerte.\"}");
            return;
        }

        try (Connection con = DatabaseConnection.getConnection()) {
            // Lösche den Termin aus appointmentsApp (nur, wenn er zum angemeldeten Benutzer gehört)
            String deleteQuery = "DELETE FROM appointmentsApp WHERE appointment_id = ? AND user_id = ?";
            try (PreparedStatement stmt = con.prepareStatement(deleteQuery)) {
                stmt.setInt(1, appointmentId);
                stmt.setInt(2, userId);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    // Aktualisiere center_time_status, booked_count um 1 dekrementieren
                    String updateStatus = "UPDATE center_time_status SET booked_count = booked_count - 1 " +
                            "WHERE center_id = ? AND time_slot_id = ? AND date1 = ?";
                    try (PreparedStatement psStatus = con.prepareStatement(updateStatus)) {
                        psStatus.setInt(1, centerId);
                        psStatus.setInt(2, timeSlotId);
                        psStatus.setString(3, date1);
                        psStatus.executeUpdate();
                    }

                    // Aktualisiere center_vaccine, fixed_quantity um 1 erhöhen
                    String updateVaccine = "UPDATE center_vaccine SET fixed_quantity = fixed_quantity + 1 " +
                            "WHERE center_id = ? AND vaccine_id = ?";
                    try (PreparedStatement psVaccine = con.prepareStatement(updateVaccine)) {
                        psVaccine.setInt(1, centerId);
                        psVaccine.setInt(2, vaccineId);
                        psVaccine.executeUpdate();
                    }

                    PrintWriter out = response.getWriter();
                    out.println("{\"status\":\"success\",\"message\":\"Termin erfolgreich gelöscht.\"}");
                } else {
                    PrintWriter out = response.getWriter();
                    out.println("{\"status\":\"error\",\"message\":\"Termin konnte nicht gelöscht werden.\"}");
                }
            }
            DatabaseConnection.releaseConnection(con);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = response.getWriter();
            out.println("{\"status\":\"error\",\"message\":\"Datenbankfehler: " + e.getMessage() + "\"}");
        }
    }
}
