package hbv.web;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.HexFormat;


public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

   /* @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect("login.html");
    }*/

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");

        String benutzername = request.getParameter("benutzername");
        String passwort = request.getParameter("passwort");

        if (benutzername == null || benutzername.isEmpty() || passwort == null || passwort.isEmpty()) {
            response.getWriter().println("<h3>Bitte füllen Sie alle Felder aus.</h3>");
            return;
        }

        if ("Admin".equals(benutzername) && "Adminpwd123".equals(passwort)) {
            response.sendRedirect("admin.html");
            return;
        }

      
        String sql = "SELECT id, name, vorname, `alter`, email, passwort, benutzername, adresse, postleitzahl, stadt " +
                "FROM users WHERE benutzername = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, benutzername);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("passwort");
                    String[] parts = storedHash.split(":");
                    if (parts.length != 2) {
                        response.getWriter().println("<h3>Interner Fehler: Ungültiges Passwortformat.</h3>");
                        return;
                    }
                    byte[] salt = HexFormat.of().parseHex(parts[0]);
                    byte[] hash = HexFormat.of().parseHex(parts[1]);

                    byte[] providedHash = hashPassword(passwort, salt);
                    if (compareHashes(hash, providedHash)) {
                        // Login reussit: creation de session
                        HttpSession session = request.getSession();
                        int id = rs.getInt("id");
                        // sauvegarde l'id dans la session
                        session.setAttribute("user_id", id);
                        //session.setAttribute("benutzername", benutzername);
                        session.setAttribute("benutzername", rs.getString("benutzername"));
                        session.setAttribute("name", rs.getString("name"));
                        session.setAttribute("vorname", rs.getString("vorname"));
                        session.setAttribute("alter", rs.getInt("alter"));
                        session.setAttribute("email", rs.getString("email"));
                        session.setAttribute("adresse", rs.getString("adresse"));
                        session.setAttribute("postleitzahl", rs.getString("postleitzahl"));
                        session.setAttribute("stadt", rs.getString("stadt"));
                        
                        response.sendRedirect("willkommen.html");
                        return;
                    } else {
                        response.getWriter().println("<h3>Falscher Benutzername oder Passwort.</h3>");
                        return;
                    }
                } else {
                    response.getWriter().println("<h3>Falscher Benutzername oder Passwort.</h3>");
                    return;
                }
            }
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            response.getWriter().println("<h3>Interner Fehler: " + e.getMessage() + "</h3>");
            e.printStackTrace();
        }
    }

    private boolean compareHashes(byte[] hash1, byte[] hash2) {
        if (hash1.length != hash2.length) {
            return false;
        }
        for (int i = 0; i < hash1.length; i++) {
            if (hash1[i] != hash2[i])
                return false;
        }
        return true;
    }

 
    private static byte[] hashPassword(String passwort, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        int PBKDF2_ITERATIONS = 210000;
        int PBKDF2_KEY_LENGTH = 256;
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        PBEKeySpec spec = new PBEKeySpec(passwort.toCharArray(), salt, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH);
        SecretKey secretKey = secretKeyFactory.generateSecret(spec);
        spec.clearPassword();
        return secretKey.getEncoded();
    }
}
