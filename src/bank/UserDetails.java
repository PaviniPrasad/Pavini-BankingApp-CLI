package bank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

//==================== USER DETAILS ====================
class UserDetails {
	private int acc_no;
	private String name;
	private String username;
	private String address;
	private String email;
	private String phone;
	private int balance;

	public UserDetails(int acc_no, String name, String username, String address, String email, String phone,
			int balance) {
		this.acc_no = acc_no;
		this.name = name;
		this.username = username;
		this.address = address;
		this.email = email;
		this.phone = phone;
		this.balance = balance;
	}

	public int getAcc_no() {
		return acc_no;
	}

	public String getName() {
		return name;
	}

	public String getUsername() {
		return username;
	}

	public String getAddress() {
		return address;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public int getBalance() {
		return balance;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

//Update a single field securely
	public boolean updateProfileField(String field, String value) {
		// Validate email and phone
		if (field.equals("email") && !SecurityUtils.isValidEmail(value)) {
			System.out.println("Invalid email format.");
			return false;
		}
		if (field.equals("phone") && !SecurityUtils.isValidPhone(value)) {
			System.out.println("Invalid phone format.");
			return false;
		}

		String query = "UPDATE user SET " + field + "=? WHERE acc_no=?";
		try (Connection con = ConnectToDB.getConnection(); PreparedStatement pstmt = con.prepareStatement(query)) {
			pstmt.setString(1, value);
			pstmt.setInt(2, acc_no);
			return pstmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
