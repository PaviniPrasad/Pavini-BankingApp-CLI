package bank;

import java.io.Console;
import java.sql.*;
import java.util.Scanner;

public class BankApp {

    // -------------------------- CONFIG --------------------------
    private static final int SCREEN_WIDTH = 80;          // used for centering
    private static final int ANIM_DELAY_MS = 80;         // header animation speed
    private static final int SPINNER_DELAY_MS = 80;      // spinner speed
    private static final int PROGRESS_DELAY_MS = 30;     // progress bar animation speed
    // ANSI color/style codes
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String DIM = "\u001B[2m";
    private static final String INV = "\u001B[7m";
    private static final String FG_CYAN = "\u001B[36m";
    private static final String FG_MAG = "\u001B[35m";
    private static final String FG_GREEN = "\u001B[32m";
    private static final String FG_YELLOW = "\u001B[33m";
    private static final String FG_RED = "\u001B[31m";
    private static final String BG_BLUE = "\u001B[44m";
    private static final String HIDE_CURSOR = "\u001B[?25l";
    private static final String SHOW_CURSOR = "\u001B[?25h";
    private static final String SAVE_CURSOR = "\u001B[s";
    private static final String RESTORE_CURSOR = "\u001B[u";
    private static final String CLEAR_LINE = "\u001B[2K";
    // ------------------------------------------------------------

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try {
            // Top-level loop to allow multiple logins in a single run
            while (true) {
                clearScreen();
                animateHeader("✨  Welcome to ABC Bank CLI  ✨");
                // Login loop
                boolean loggedin = false;
                UserDetails user = null;
                while (!loggedin) {
                    printBoxedCentered("Please login to continue", 60);
                    System.out.print(FG_CYAN + "Username: " + RESET);
                    String username = sc.nextLine().trim();

                    Login login = new Login(); // uses existing class
                    if (login.doesUsernameExist(username)) {
                    	String password;
                    	Console console = System.console();
                    	if (console != null) {
                    	    char[] pwdChars = console.readPassword(FG_CYAN + "Password: " + RESET);
                    	    password = new String(pwdChars);
                    	} else {
                    	    // fallback if console is null (IDE, some terminals)
                    	    System.out.print(FG_CYAN + "Password: " + RESET);
                    	    password = sc.nextLine();
                    	}

                        showSpinnerAsync("Authenticating", 8);
                        if (login.isPasswordCorrect(password)) {
                            notifyOverlay(FG_GREEN + "Login successful!" + RESET, 900);
                            loggedin = true;
                            user = login.getUserDetails();
                        } else {
                            notifyOverlay(FG_RED + "Wrong password." + RESET, 900);
                        }
                    } else {
                        notifyOverlay(FG_YELLOW + "Username not found." + RESET, 900);
                    }
                }

                // Welcome banner
                clearScreen();
                printHeaderBanner("Welcome " + user.getName() + " to ABC Bank");
                boolean sessionActive = true;
                Money money = new Money(user.getAcc_no()); // existing class

                while (sessionActive) {
                    drawMainMenu(user.getName());
                    System.out.print(FG_MAG + "Enter your choice: " + RESET);
                    String choice = sc.nextLine().trim();

                    switch (choice) {
                        case "1": // deposit
                            try {
                                System.out.print(FG_CYAN + "Enter deposit amount: " + RESET);
                                int depositAmount = Integer.parseInt(sc.nextLine().trim());
                                if (!confirmOverlay("Confirm deposit of " + depositAmount + " ? (Y/N)")) {
                                    notifyOverlay(DIM + "Deposit cancelled." + RESET, 800);
                                    break;
                                }
                                boolean success = simulateMoneyOperationWithProgress("Depositing", () -> money.deposit(depositAmount));
                                if (success) {
                                    notifyOverlay(FG_GREEN + "Deposit successful. New balance: " + money.getBalance() + RESET, 1400);
                                } else {
                                    notifyOverlay(FG_RED + "Deposit failed." + RESET, 1000);
                                }
                            } catch (NumberFormatException ex) {
                                notifyOverlay(FG_RED + "Invalid amount entered." + RESET, 1000);
                            }
                            break;

                        case "2": // withdrawal
                            try {
                                System.out.print(FG_CYAN + "Enter withdrawal amount: " + RESET);
                                int withdrawAmount = Integer.parseInt(sc.nextLine().trim());
                                if (!confirmOverlay("Confirm withdrawal of " + withdrawAmount + " ? (Y/N)")) {
                                    notifyOverlay(DIM + "Withdrawal cancelled." + RESET, 800);
                                    break;
                                }
                                boolean success = simulateMoneyOperationWithProgress("Withdrawing", () -> money.withdraw(withdrawAmount));
                                if (success) {
                                    notifyOverlay(FG_GREEN + "Withdrawal successful. New balance: " + money.getBalance() + RESET, 1400);
                                } else {
                                    notifyOverlay(FG_RED + "Withdrawal failed." + RESET, 1000);
                                }
                            } catch (NumberFormatException ex) {
                                notifyOverlay(FG_RED + "Invalid amount entered." + RESET, 1000);
                            }
                            break;

                        case "3": // check balance
                            showSidebarBalance(money.getBalance());
                            break;

                        case "4": // edit profile
                            showEditProfileMenu(sc, user);
                            break;

                        case "5": // change password
                            handleChangePassword(sc, user);
                            break;

                        case "6": // exit
                            sessionActive = false;
                            notifyOverlay(FG_YELLOW + "Logging out..." + RESET, 900);
                            break;

                        default:
                            notifyOverlay(FG_RED + "Invalid choice. Try again." + RESET, 900);
                    }
                }

                // Post-session prompt
                System.out.print(FG_MAG + "Do you want to login as another user? (Y/N): " + RESET);
                String cont = sc.nextLine().trim().toUpperCase();
                if (!cont.equals("Y")) {
                    notifyOverlay(FG_CYAN + "Thank you for using ABC Bank CLI. Bye!" + RESET, 1200);
                    break;
                }
            }
        } finally {
            sc.close();
            // ensure cursor shown
            System.out.print(SHOW_CURSOR + RESET);
        }
    }

    // --------------------- UI UTILITIES ---------------------

    private static void clearScreen() {
        System.out.print("\u001B[2J\u001B[H"); // clear and move cursor home
    }

    private static void animateHeader(String text) {
        // simple breathing/slide animation
        System.out.print(HIDE_CURSOR);
        int w = SCREEN_WIDTH;
        for (int i = 0; i < 12; i++) {
            clearScreen();
            int pad = Math.abs((i % (w / 6)) - (w / 12));
            printCentered(BOLD + FG_CYAN + repeat(" ", pad) + text + RESET);
            sleep(ANIM_DELAY_MS);
        }
        System.out.print(SHOW_CURSOR);
    }

    private static void printCentered(String line) {
        int pad = Math.max(0, (SCREEN_WIDTH - stripAnsi(line).length()) / 2);
        System.out.println(repeat(" ", pad) + line);
    }

    private static void printBoxedCentered(String content, int boxWidth) {
        int w = Math.min(boxWidth, SCREEN_WIDTH - 4);
        String top = "┌" + repeat("─", w - 2) + "┐";
        String bot = "└" + repeat("─", w - 2) + "┘";
        int pad = Math.max(0, (SCREEN_WIDTH - w) / 2);
        System.out.println(repeat(" ", pad) + top);
        String line = content;
        if (stripAnsi(line).length() > w - 4) {
            line = stripAnsi(line).substring(0, w - 4);
        }
        int innerPad = w - 2 - stripAnsi(line).length();
        System.out.println(repeat(" ", pad) + "│ " + line + repeat(" ", innerPad - 1) + "│");
        System.out.println(repeat(" ", pad) + bot);
    }

    private static void printHeaderBanner(String title) {
        clearScreen();
        System.out.println(BG_BLUE + " " + repeat(" ", SCREEN_WIDTH - 2) + " " + RESET);
        printCentered(BOLD + FG_YELLOW + title + RESET);
        System.out.println();
    }

    private static void drawMainMenu(String username) {
        clearScreen();
        printHeaderBanner("ABC Bank - Main Menu");
        System.out.println(FG_CYAN + "User: " + username + RESET);
        System.out.println();
        System.out.println(boxedOption("1", "Deposit"));
        System.out.println(boxedOption("2", "Withdrawal"));
        System.out.println(boxedOption("3", "Check Balance"));
        System.out.println(boxedOption("4", "Edit Profile"));
        System.out.println(boxedOption("5", "Change Password"));
        System.out.println(boxedOption("6", "Logout"));
        System.out.println();
    }

    private static String boxedOption(String num, String label) {
        return FG_MAG + " [" + num + "] " + RESET + label;
    }

    private static void showSpinnerAsync(String message, int cycles) {
        // spinner while a short action appears to run (blocks briefly)
        String[] spin = {"|", "/", "-", "\\"};
        System.out.print(SAVE_CURSOR);
        for (int i = 0; i < cycles; i++) {
            System.out.print(CLEAR_LINE + "\r" + FG_CYAN + message + " " + spin[i % spin.length] + RESET);
            sleep(SPINNER_DELAY_MS);
        }
        System.out.print(RESTORE_CURSOR);
        System.out.println();
    }

    private static void showSidebarBalance(int balance) {
        // Overlay a small panel with balance using save/restore
        System.out.print(SAVE_CURSOR);
        System.out.print("\n" + FG_GREEN + "┌──────── Balance ────────┐\n");
        System.out.print("│ Balance: " + balance + repeat(" ", 16) + "│\n");
        System.out.print("└─────────────────────────┘" + RESET + "\n");
        System.out.print(RESTORE_CURSOR);
        // pause to let user see
        sleep(900);
    }

    private static boolean confirmOverlay(String message) {
        System.out.print(SAVE_CURSOR);
        System.out.print("\n" + INV + " " + message + " " + RESET + "\n");
        System.out.print(RESTORE_CURSOR);
        Scanner s = new Scanner(System.in);
        String resp = s.nextLine().trim().toUpperCase();
        return resp.equals("Y");
    }

    private static void notifyOverlay(String message, int millis) {
        System.out.print(SAVE_CURSOR);
        System.out.print("\n" + BG_BLUE + " " + repeat(" ", SCREEN_WIDTH - 4) + " " + RESET + "\n");
        System.out.print(RESTORE_CURSOR);
        // small inline notification
        System.out.println(message);
        sleep(millis);
    }

    private static boolean simulateMoneyOperationWithProgress(String label, RunnableWithBoolean op) {
        // Show a progress bar while executing op; op should perform the money change (synchronous)
        int total = 24;
        System.out.print(SAVE_CURSOR);
        System.out.print("\n" + label + ": [");
        for (int i = 0; i < total; i++) {
            System.out.print(" ");
        }
        System.out.print("] 0%");
        System.out.print(RESTORE_CURSOR);

        // run operation in-thread but animate progress as if it's happening
        // We'll attempt op and animate bar - we don't have async threads for op,
        // so we call op, then fake progress to completion.
        boolean result = op.run();
        // animate progress to completion
        for (int i = 0; i <= total; i++) {
            int percent = (i * 100) / total;
            System.out.print("\r" + label + ": [");
            System.out.print(repeat("=", i));
            System.out.print(repeat(" ", total - i));
            System.out.print("] " + percent + "%");
            sleep(PROGRESS_DELAY_MS);
        }
        System.out.println();
        return result;
    }

    private static void showEditProfileMenu(Scanner sc, UserDetails user) {
        System.out.println();
        System.out.println("What would you like to edit?");
        System.out.println("1. Name");
        System.out.println("2. Address");
        System.out.println("3. Email");
        System.out.println("4. Phone");
        System.out.println("5. Cancel");
        System.out.print(FG_CYAN + "Enter your choice: " + RESET);
        String editChoice = sc.nextLine().trim();
        boolean updated = false;
        switch (editChoice) {
            case "1":
                System.out.print("Enter new name: ");
                String newName = sc.nextLine();
                updated = user.updateProfileField("name", newName);
                if (updated) user.setName(newName);
                break;
            case "2":
                System.out.print("Enter new address: ");
                String newAddress = sc.nextLine();
                updated = user.updateProfileField("address", newAddress);
                if (updated) user.setAddress(newAddress);
                break;
            case "3":
                System.out.print("Enter new email: ");
                String newEmail = sc.nextLine();
                updated = user.updateProfileField("email", newEmail);
                if (updated) user.setEmail(newEmail);
                break;
            case "4":
                System.out.print("Enter new phone: ");
                String newPhone = sc.nextLine();
                updated = user.updateProfileField("phone", newPhone);
                if (updated) user.setPhone(newPhone);
                break;
            case "5":
                notifyOverlay(DIM + "Edit cancelled." + RESET, 700);
                break;
            default:
                notifyOverlay(FG_RED + "Invalid choice." + RESET, 700);
        }
        if (updated) {
            notifyOverlay(FG_GREEN + "Profile updated successfully." + RESET, 900);
        } else if (!editChoice.equals("5")) {
            notifyOverlay(FG_RED + "Profile update failed." + RESET, 900);
        }
    }

    private static void handleChangePassword(Scanner sc, UserDetails user) {
        Console console = System.console();
        String currentPass, newPass, confirmPass;

        if (console != null) {
            currentPass = new String(console.readPassword("Enter your current password: "));
        } else {
            System.out.print("Enter your current password: ");
            currentPass = sc.nextLine();
        }

        Login login = new Login(user.getUsername());
        if (!login.isPasswordCorrect(currentPass)) {
            notifyOverlay(FG_RED + "Incorrect current password. Password change failed." + RESET, 1200);
            return;
        }

        if (console != null) {
            newPass = new String(console.readPassword("Enter your new password: "));
            confirmPass = new String(console.readPassword("Confirm your new password: "));
        } else {
            System.out.print("Enter your new password: ");
            newPass = sc.nextLine();
            System.out.print("Confirm your new password: ");
            confirmPass = sc.nextLine();
        }

        if (!newPass.equals(confirmPass)) {
            notifyOverlay(FG_RED + "Passwords do not match. Password change failed." + RESET, 1200);
            return;
        }

        boolean pwdChanged = login.changePassword(user.getAcc_no(), newPass);
        if (pwdChanged) {
            notifyOverlay(FG_GREEN + "Password changed successfully." + RESET, 1200);
        } else {
            notifyOverlay(FG_RED + "Password change failed." + RESET, 1200);
        }
    }


    // ---------------------- SMALL HELPERS ----------------------

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(s);
        return sb.toString();
    }

    // remove ANSI sequences roughly for length calculations
    private static String stripAnsi(String s) {
        return s.replaceAll("\u001B\\[[;\\d]*m", "");
    }

    @FunctionalInterface
    private interface RunnableWithBoolean {
        boolean run();
    }
}
