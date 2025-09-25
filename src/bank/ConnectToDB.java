package bank;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//==================== CONNECT TO DB ====================
class ConnectToDB {
 private static final String URL = "jdbc:mysql://localhost:3306/abc_bank";
 private static final String USER = "root";
 private static final String PASSWORD = "1234";

 public static Connection getConnection() {
     try {
         return DriverManager.getConnection(URL, USER, PASSWORD);
     } catch (SQLException e) {
         e.printStackTrace();
         throw new RuntimeException("Unable to connect to DB");
     }
 }
}
