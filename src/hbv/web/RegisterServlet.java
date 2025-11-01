package hbv.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HexFormat;


public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;


  /*  @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
               response.sendRedirect("register.html");
    }*/

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Zeichensatz setzen
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        PrintWriter pr = response.getWriter();

        String name = request.getParameter("name");
        String vorname = request.getParameter("vorname");
        String alter = request.getParameter("alter");
        String email = request.getParameter("email");
        String passwort = request.getParameter("passwort");
        String benutzername = request.getParameter("benutzername");
        String adresse = request.getParameter("adresse");
        String postleitzahl = request.getParameter("postleitzahl");
        String stadt = request.getParameter("stadt");

     
        if (name == null || name.isEmpty() ||
                vorname == null || vorname.isEmpty() ||
                alter == null || alter.isEmpty() ||
                email == null || email.isEmpty() ||
                passwort == null || passwort.isEmpty() ||
                benutzername == null || benutzername.isEmpty() ||
                adresse == null || adresse.isEmpty() ||
                postleitzahl == null || postleitzahl.isEmpty() ||
                stadt == null || stadt.isEmpty()) {
            pr.println("<h3>Alle Felder müssen ausgefüllt werden</h3>");
            return;
        }

        // Hash du mot de pass avec PBKDF2
        String hashedPassword;
        try {
            byte[] salt = generateSalt();
            byte[] hash = hashPassword(passwort, salt);
            // sauvegarde en "salt:hash" (coder en hexa)
            hashedPassword = HexFormat.of().formatHex(salt) + ":" + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            pr.println("<h3>Fehler beim Hashen: " + e.getMessage() + "</h3>");
            return;
        }

        // connection á la bd
        try (Connection conn = DatabaseConnection.getConnection()) {

            
            try (PreparedStatement checkStatement = conn.prepareStatement("SELECT * FROM users WHERE email=? OR benutzername=?")) {
                checkStatement.setString(1, email);
                checkStatement.setString(2, benutzername);

                try (ResultSet resultSet = checkStatement.executeQuery()) {
                    if (resultSet.next()) {
                        pr.println("<h3>E-Mail oder Benutzername existiert bereits</h3>");
                        return;
                    }
                }
            }

            // Ajouter l'utilisateur á la bd
            try (PreparedStatement insertStatement = conn.prepareStatement(
                    "INSERT INTO users (name, vorname, `alter`, email, passwort, benutzername, adresse, postleitzahl, stadt) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

                insertStatement.setString(1, name);
                insertStatement.setString(2, vorname);
                insertStatement.setInt(3, Integer.parseInt(alter));
                insertStatement.setString(4, email);
                insertStatement.setString(5, hashedPassword);
                insertStatement.setString(6, benutzername);
                insertStatement.setString(7, adresse);
                insertStatement.setString(8, postleitzahl);
                insertStatement.setString(9, stadt);

                int rowsInserted = insertStatement.executeUpdate();
                if (rowsInserted > 0) {
                    //pr.println("<h3>Registrierung erfolgreich!</h3>");
                    response.sendRedirect("login.html");
                } else {
                    pr.println("<h3>Registrierung fehlgeschlagen</h3>");
                }
            }

        } catch (SQLException e) {
            pr.println("<h3>Datenbankfehler: " + e.getMessage() + "</h3>");
            e.printStackTrace();
        }
    }

    // creation du salt alleatoir
    private static byte[] generateSalt() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstanceStrong();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    // Methode Hash PBKDF2
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
