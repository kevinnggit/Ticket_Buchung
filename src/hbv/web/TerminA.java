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

public class TerminA extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("login.html");
            return;
        }
        Integer userId = (Integer) session.getAttribute("user_id");
        if (userId == null) {
            response.sendRedirect("login.html");
            return;
        }

        // Requête SQL pour récupérer les rendez-vous de l'utilisateur, en incluant le nom du centre
        //String sql = "SELECT a.appointment_id, a.center_id, vc.name AS center_name, a.date1, a.heure, a.vaccine_id, a.time_slot_id " +
        //        "FROM appointmentsApp a " +
        //        "JOIN vaccination_center vc ON a.center_id = vc.center_id " +
        //        "WHERE a.user_id = ?";
        

        String sql = "SELECT a.appointment_id, a.center_id, vc.name AS center_name, a.date1, a.heure, a.vaccine_id, v.name AS vaccine_name, a.time_slot_id " +
        "FROM appointmentsApp a " +
        "JOIN vaccination_center vc ON a.center_id = vc.center_id " +
        "JOIN vaccine v ON a.vaccine_id = v.vaccine_id " +
        "WHERE a.user_id = ?";


        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
              // Récupère le flux de sortie pour écrire la réponse HTML
                PrintWriter out = response.getWriter();

                out.println("<html>");
                out.println("<head>");
                out.println("<title>Meine Termine</title>");
                out.println("<link rel='stylesheet' type='text/css' href='script.css'>");
                
                out.println("<style>");
                out.println("table { border-collapse: collapse; width: 100%; font-family: Arial, sans-serif; font-size: 14px; }");
                out.println("th, td { text-align: left; padding: 8px; border-bottom: 1px solid #ddd; }");
                out.println("th { background-color: #008CBA; color: white; }");
                out.println("tr:nth-child(even) { background-color: #f2f2f2; }");
                out.println("</style>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Meine Termine</h1>");

                out.println("<table border='1'>");
                out.println("<tr>");
                out.println("<th>Zentrum</th>");
                out.println("<th>Datum</th>");
                out.println("<th>Uhrzeit</th>");
                out.println("<th>Impfstoff</th>");
                out.println("<th>Termin stornieren</th>");
                out.println("</tr>");

                // Boucle sur chaque rendez-vous récupéré dans le ResultSet
                while (rs.next()) {
                    int appointmentId = rs.getInt("appointment_id");
                    int centerId = rs.getInt("center_id");
                    String centerName = rs.getString("center_name");
                    String dateStr = rs.getString("date1");
                    String heure = rs.getString("heure");
                    int vaccineId = rs.getInt("vaccine_id");
                    String vaccineName = rs.getString("vaccine_name");
                    int timeSlotId = rs.getInt("time_slot_id");

                    // // Chaque ligne est identifiée et dotée d'attributs de données
                    out.println("<tr id='appointment_" + appointmentId + "' " +
                            "data-center_id='" + centerId + "' " +
                            "data-date1='" + dateStr + "' " +
                            "data-time_slot_id='" + timeSlotId + "' " +
                            "data-vaccine_id='" + vaccineId + "'>");
                    out.println("<td>" + centerName + "</td>");
                    out.println("<td>" + dateStr + "</td>");
                    out.println("<td>" + heure + "</td>");
                    out.println("<td>" + vaccineName + "</td>");
                    //out.println("<td>" + vaccineId + "</td>");
                    out.println("<td>");
                    out.println("<button onclick=\"deleteAppointment(" + appointmentId + ", document.getElementById('appointment_" + appointmentId + "'))\">Stornieren</button>");
                    out.println("</td>");
                    out.println("</tr>");
                }

                out.println("</table>");
                out.println("</body>");
                out.println("</html>");
            }
            DatabaseConnection.releaseConnection(con);
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("<p>Datenbankfehler: " + e.getMessage() + "</p>");
        }
    }
}
