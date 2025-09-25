package bank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PasswordMigrator {
    /*public static void main(String[] args) {
        String selectQuery = "SELECT acc_no, password FROM user";
        String updateQuery = "UPDATE user SET password=? WHERE acc_no=?";
        try (Connection con = ConnectToDB.getConnection();
             PreparedStatement selectStmt = con.prepareStatement(selectQuery);
             PreparedStatement updateStmt = con.prepareStatement(updateQuery)) {

            ResultSet rs = selectStmt.executeQuery();
            while (rs.next()) {
                int acc_no = rs.getInt("acc_no");
                String plainPassword = rs.getString("password");
                String hashed = SecurityUtils.hashPassword(plainPassword);

                updateStmt.setString(1, hashed);
                updateStmt.setInt(2, acc_no);
                updateStmt.executeUpdate();
                System.out.println("Password hashed for account: " + acc_no);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }*/
}

