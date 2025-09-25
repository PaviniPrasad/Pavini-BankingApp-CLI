package bank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//==================== MONEY ====================
class Money {
	private int acc_no;
	private int balance;
	private static final int MIN_BALANCE = 10000;

	public Money(int acc_no) {
		this.acc_no = acc_no;
		fetchBalanceFromDB();
	}

	private void fetchBalanceFromDB() {
		String query = "SELECT balance FROM user WHERE acc_no = ?";
		try (Connection con = ConnectToDB.getConnection(); PreparedStatement pstmt = con.prepareStatement(query)) {
			pstmt.setInt(1, acc_no);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				this.balance = rs.getInt("balance");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int getBalance() {
		return balance;
	}

	public boolean deposit(int amount) {
		if (amount <= 0) {
			System.out.println("Amount should be positive.");
			return false;
		}
		int newBalance = this.balance + amount;
		return updateBalanceInDB(newBalance);
	}

	public boolean withdraw(int amount) {
		if (amount <= 0) {
			System.out.println("Amount should be positive.");
			return false;
		}
		if (this.balance - amount < MIN_BALANCE) {
			System.out.println("Insufficient funds. Minimum balance must be " + MIN_BALANCE);
			return false;
		}
		int newBalance = this.balance - amount;
		return updateBalanceInDB(newBalance);
	}

	private boolean updateBalanceInDB(int newBalance) {
		String query = "UPDATE user SET balance = ? WHERE acc_no = ?";
		try (Connection con = ConnectToDB.getConnection(); PreparedStatement pstmt = con.prepareStatement(query)) {
			pstmt.setInt(1, newBalance);
			pstmt.setInt(2, acc_no);
			if (pstmt.executeUpdate() == 1) {
				this.balance = newBalance;
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
}
