package hbv.web;

import hbv.web.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.RequestDispatcher;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;


public class BuchungServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

   /* @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect("zentren.html");
    }*/

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("login.html");
            return;
        }

        // Recuperation des Infos Utilisatieur en contactant directement la session
        //int userId = (Integer) session.getAttribute("id");
        Integer userIdObj = (Integer) session.getAttribute("user_id");
        if (userIdObj == null) {
            response.getWriter().println("<h2>Benutzer-ID nicht gefunden. Bitte loggen Sie sich erneut ein.</h2>");
            return;
        }
        int userId = userIdObj.intValue();
        String benutzername = (String) session.getAttribute("benutzername");

        String emailAddress = (String) session.getAttribute("email");

        // Vérifie si l'utilisateur a déjà réservé 4 rendez-vous
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement psCount = con.prepareStatement("SELECT COUNT(*) FROM appointmentsApp WHERE user_id = ?")) {
            psCount.setInt(1, userId);
            try (ResultSet rsCount = psCount.executeQuery()) {
                if (rsCount.next()) {
                    int count = rsCount.getInt(1);
                    if (count >= 4) {
                        PrintWriter out = response.getWriter();
                        out.println("<html><head><script>");
                        out.println("alert('Sie können nicht mehr als 4 Termine buchen.');");
                        out.println("setTimeout(function(){ window.location.href='zentren.html'; }, 3000);");
                        out.println("</script></head><body></body></html>");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            PrintWriter out = response.getWriter();
            out.println("<h2>Datenbankfehler beim Prüfen der Terminzahl: " + e.getMessage() + "</h2>");
            return;
        }

        // Recuperer les Info du Formulaire
        String centerName = request.getParameter("center");
        String date1 = request.getParameter("date1");
        String heureStr = request.getParameter("heure1");
        String vaccineValue = request.getParameter("vaccine");
        String familienMitglied = request.getParameter("familien_mitglied");
        String relation = request.getParameter("relation");

        // Remplire tout ce qui est obligatoir
        if (centerName == null || centerName.isEmpty() ||
                date1 == null || date1.isEmpty() ||
                heureStr == null || heureStr.isEmpty() ||
                vaccineValue == null || vaccineValue.isEmpty()) {
            response.getWriter().println("<h2>Bitte alle Pflichtfelder ausfüllen.</h2>");
            return;
        }

        // verifier si la date est passeé ou si le format ne correspond pas
        LocalDate chosenDate;
        try {
            chosenDate = LocalDate.parse(date1);
            if (chosenDate.isBefore(LocalDate.now())) {
                response.getWriter().println("<h2>Das ausgewählte Datum liegt in der Vergangenheit.</h2>");
                return;
            }
        } catch (DateTimeParseException e) {
            response.getWriter().println("<h2>Ungültiges Datumsformat.</h2>");
            return;
        }

        try (Connection con = DatabaseConnection.getConnection()) {
            // recuperer l'id du centre et le nombre de Cabines
            int centerId = 0;
            int cabins = 0;
            try (PreparedStatement psCenter = con.prepareStatement(
                    "SELECT center_id, cabins FROM vaccination_center WHERE name = ?")) {
                psCenter.setString(1, centerName);
                try (ResultSet rsCenter = psCenter.executeQuery()) {
                    if (rsCenter.next()) {
                        centerId = rsCenter.getInt("center_id");
                        cabins = rsCenter.getInt("cabins");
                    } else {
                        response.getWriter().println("<h2>Zentrum nicht gefunden.</h2>");
                        return;
                    }
                }
            }

            // Recupere L'id de l'heure selectionner
            // On s'assure que l'heure est au format HH:MM:SS on complete alor la ligne recu avec ":00".
            String timeValue = heureStr;
            if (!timeValue.contains(":")) {
                response.getWriter().println("<h2>Ungültige Uhrzeit.</h2>");
                return;
            }
            if (timeValue.split(":").length == 2) {
                timeValue += ":00";
            }
            int timeSlotId = 0;
            try (PreparedStatement psTime = con.prepareStatement(
                    "SELECT time_slot_id FROM time_slot WHERE slot_value = ?")) {
                psTime.setString(1, timeValue);
                try (ResultSet rsTime = psTime.executeQuery()) {
                    if (rsTime.next()) {
                        timeSlotId = rsTime.getInt("time_slot_id");
                    } else {
                        response.getWriter().println("<h2>Uhrzeit nicht gefunden.</h2>");
                        return;
                    }
                }
            }

           
            int vaccineId = Integer.parseInt(vaccineValue);

            //Vérifier la disponibilité du créneau horaire dans le centre (en comparant booked_count et le nombre de cabines)
            int bookedCount = 0;
            try (PreparedStatement psStatus = con.prepareStatement(
                    "SELECT booked_count FROM center_time_status WHERE center_id = ? AND time_slot_id = ? AND date1 = ?")) {
                psStatus.setInt(1, centerId);
                psStatus.setInt(2, timeSlotId);
                psStatus.setString(3, date1);
                try (ResultSet rsStatus = psStatus.executeQuery()) {
                    if (rsStatus.next()) {
                        bookedCount = rsStatus.getInt("booked_count");
                    }
                }
            }
            if (bookedCount >= cabins) {
                // Sende eine Fehlermeldung per JavaScript, die nach 3 Sekunden verschwindet.
                PrintWriter out = response.getWriter();
                out.println("<html><head><script>");
                out.println("alert('Die gewählte Uhrzeit ist nicht verfügbar. Bitte wählen Sie eine andere.');");
                out.println("setTimeout(function(){ window.location.href='zentren.html'; }, 3000);");
                out.println("</script></head><body></body></html>");
                return;
            }

            // Vérifier la disponibilité du vaccin dans le centre
            int availableVaccine = 0;
            try (PreparedStatement psVaccine = con.prepareStatement(
                    "SELECT fixed_quantity FROM center_vaccine WHERE center_id = ? AND vaccine_id = ?")) {
                psVaccine.setInt(1, centerId);
                psVaccine.setInt(2, vaccineId);
                try (ResultSet rsVaccine = psVaccine.executeQuery()) {
                    if (rsVaccine.next()) {
                        availableVaccine = rsVaccine.getInt("fixed_quantity");
                    } else {
                        response.getWriter().println("<h2>Der gewählte Impfstoff ist in diesem Zentrum nicht verfügbar.</h2>");
                        return;
                    }
                }
            }
            if (availableVaccine <= 0) {
                PrintWriter out = response.getWriter();
                out.println("<html><head><script>");
                out.println("alert('Der gewählte Impfstoff ist ausgebucht.');");
                out.println("setTimeout(function(){ window.location.href='zentren.html'; }, 3000);");
                out.println("</script></head><body></body></html>");
                return;
            }

            // Sauvegarde le RDV
            String insertQuery = "INSERT INTO appointmentsApp (center_id, user_id, date1, time_slot_id, heure, vaccine_id, qr_code_path, familien_mitglied, relation) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psInsert = con.prepareStatement(insertQuery)) {
                psInsert.setInt(1, centerId);
                psInsert.setInt(2, userId);
                psInsert.setString(3, date1);
                psInsert.setInt(4, timeSlotId);
                psInsert.setString(5, timeValue);
                psInsert.setInt(6, vaccineId);
                psInsert.setString(7, "");
                psInsert.setString(8, familienMitglied);
                psInsert.setString(9, relation);
                int rowsAffected = psInsert.executeUpdate();
                if (rowsAffected > 0) {

                    // Mise à jour de la table center_time_status :
                    // Si une entrée existe pour ce créneau, on incrémente le compteur, sinon on crée une nouvelle entrée
                    String selectStatus = "SELECT booked_count FROM center_time_status WHERE center_id = ? AND time_slot_id = ? AND date1 = ?";
                    try (PreparedStatement psStatusSelect = con.prepareStatement(selectStatus)) {
                        psStatusSelect.setInt(1, centerId);
                        psStatusSelect.setInt(2, timeSlotId);
                        psStatusSelect.setString(3, date1);
                        try (ResultSet rsStatusSelect = psStatusSelect.executeQuery()) {
                            if (rsStatusSelect.next()) {
                                // Existe: Mise á jour
                                String updateStatus = "UPDATE center_time_status SET booked_count = booked_count + 1 WHERE center_id = ? AND time_slot_id = ? AND date1 = ?";
                                try (PreparedStatement psUpdate = con.prepareStatement(updateStatus)) {
                                    psUpdate.setInt(1, centerId);
                                    psUpdate.setInt(2, timeSlotId);
                                    psUpdate.setString(3, date1);
                                    psUpdate.executeUpdate();
                                }
                            } else {
                                // si non creation
                                String insertStatus = "INSERT INTO center_time_status (center_id, time_slot_id, date1, booked_count) VALUES (?, ?, ?, ?)";
                                try (PreparedStatement psInsertStatus = con.prepareStatement(insertStatus)) {
                                    psInsertStatus.setInt(1, centerId);
                                    psInsertStatus.setInt(2, timeSlotId);
                                    psInsertStatus.setString(3, date1);
                                    psInsertStatus.setInt(4, 1);
                                    psInsertStatus.executeUpdate();
                                }
                            }
                        }
                    }

                    // Reduire de 1 la quantité vaccin dans center_vaccine
                    String updateVaccine = "UPDATE center_vaccine SET fixed_quantity = fixed_quantity - 1 WHERE center_id = ? AND vaccine_id = ?";
                    try (PreparedStatement psUpdateVaccine = con.prepareStatement(updateVaccine)) {
                        psUpdateVaccine.setInt(1, centerId);
                        psUpdateVaccine.setInt(2, vaccineId);
                        psUpdateVaccine.executeUpdate();
                    }

                    String sqlGetAppointmentId =
                            "SELECT appointment_id " +
                                    "FROM appointmentsApp " +
                                    "WHERE user_id = ? " +
                                    "ORDER BY booking_time DESC " +
                                    "LIMIT 1";

                    try (PreparedStatement stmtGetId = con.prepareStatement(sqlGetAppointmentId)) {
                        stmtGetId.setInt(1, userId);

                        try (ResultSet rs = stmtGetId.executeQuery()) {
                            if (rs.next()) {
                                int appointmentId = rs.getInt("appointment_id");

                                // Hier rufen wir SendMail auf und übergeben alle nötigen Informationen
                                // On transmet l'ID du rendez-vous, le nom du centre, la date, l'heure et le vaccin
                                SendMail mailSender = new SendMail();
                                mailSender.sendConfirmation(
                                        String.valueOf(appointmentId),  // Termin-ID
                                        centerName,                     // Zentrum
                                        date1,                          // Datum
                                        timeValue,                      // Zeitslot
                                        vaccineValue,                   // Impfstoff
                                        String.valueOf(userId)          // UserID
                                );
                            } else {
                                response.getWriter().println("<h2>Konnte keine Termin-ID ermitteln.</h2>");
                            }
                        }
                    }


                    response.sendRedirect("termine.html");
                } else {
                    response.getWriter().println("<h2>Ihr Termin konnte nicht gespeichert werden. Bitte versuchen Sie es später erneut.</h2>");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            PrintWriter out = response.getWriter();
            out.println("<h2>Datenbankfehler: " + e.getMessage() + "</h2>");
        }
    }
}
