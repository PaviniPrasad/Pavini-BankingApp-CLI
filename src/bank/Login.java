package bank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//==================== LOGIN ====================
class Login {
	private String username;

	public Login() {
	}

	public Login(String username) {
		this.username = username;
	}

	public boolean doesUsernameExist(String username) {
		this.username = username;
		String query = "SELECT 1 FROM user WHERE username = ?";
		try (Connection con = ConnectToDB.getConnection(); PreparedStatement pstmt = con.prepareStatement(query)) {
			pstmt.setString(1, username);
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isPasswordCorrect(String password) {
		String query = "SELECT password FROM user WHERE username = ?";
		try (Connection con = ConnectToDB.getConnection(); PreparedStatement pstmt = con.prepareStatement(query)) {
			pstmt.setString(1, username);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				String storedHash = rs.getString("password");
				return SecurityUtils.hashPassword(password).equals(storedHash);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public UserDetails getUserDetails() {
		String query = "SELECT acc_no, name, username, address, email, phone, balance FROM user WHERE username = ?";
		try (Connection con = ConnectToDB.getConnection(); PreparedStatement pstmt = con.prepareStatement(query)) {
			pstmt.setString(1, this.username);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return new UserDetails(rs.getInt("acc_no"), rs.getString("name"), rs.getString("username"),
						rs.getString("address"), rs.getString("email"), rs.getString("phone"), rs.getInt("balance"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean changePassword(int acc_no, String newPassword) {
		String hashed = SecurityUtils.hashPassword(newPassword);
		String query = "UPDATE user SET password=? WHERE acc_no=?";
		try (Connection con = ConnectToDB.getConnection(); PreparedStatement pstmt = con.prepareStatement(query)) {
			pstmt.setString(1, hashed);
			pstmt.setInt(2, acc_no);
			return pstmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
