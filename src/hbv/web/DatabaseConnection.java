package hbv.web;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {

    private static HikariDataSource dataSource;

    static {
        // Création d'un objet HikariConfig pour configurer le pool
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mariadb://mysql-server:3306/swe3-2024-08_db");
        config.setUsername("swe3-2024-08");
        config.setPassword("QbUk49wrEdmXBZ6BQBWC");

        // Configuration des paramètres du pool de connexions
        config.setMaximumPoolSize(100);
        config.setMinimumIdle(10);
        config.setConnectionTimeout(10_000); // 10 sec
        config.setIdleTimeout(600_000);        // 10 Min
        config.setMaxLifetime(1_800_000);      // 30 Min

        // Optional: verifier si les connections sont valides avant de les zurückgeben
        config.setValidationTimeout(5000);     // 5 Sec
        config.setLeakDetectionThreshold(2000); // 2 Sec detection des Leaks

        //Création d'un Pool HikariDataSource avec la configuration définie
        dataSource = new HikariDataSource(config);
    }

    
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    
    public static void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException("Fehler beim Freigeben der Verbindung", e);
            }
        }
    }
}
