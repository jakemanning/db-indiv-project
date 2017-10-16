import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class OptionManager {

    private final Connection connection;
    private Scanner scanner;

    public OptionManager(final Connection connection) throws SQLException {
        this.connection = connection;
    }

    public void handleOptions() throws SQLException {
        try(final Scanner scanner = new Scanner(System.in)) {
            this.scanner = scanner;
            int input = getUserOption();
            while(input != Option.QUIT.value) {
                handleInput(input);
                input = getUserOption();
            }
            System.out.println("Thank you for using BoomerSQLer");
        }
    }

    private int getUserOption() {
        try {
            System.out.print("Enter a number (1-4): ");
            final int input = scanner.nextInt();
            scanner.nextLine();
            return input;
        } catch(InputMismatchException i) {
            scanner.nextLine();
            return -1;
        }
    }

    private void handleInput(final int input) throws SQLException {
        if(input == Option.INSERT.value) {
            insertCustomer();
        } else if(input == Option.COMPENSATE.value) {
            compensateTranslators();
        } else if(input == Option.DISPLAY.value) {
            display();
        } else {
            System.out.println("Invalid input.");
        }
    }

    private void insertCustomer() throws SQLException {
        final String cid = getProcedureArgs("Enter cid");
        final String cname = getProcedureArgs("Enter cname");
        final String clevel = getProcedureArgs("Enter clevel");

        try(final CallableStatement pstmt = connection.prepareCall("{call book_package.insert_customer(?,?,?)}")) {
            pstmt.setString(1, cid);
            pstmt.setString(2, cname);
            pstmt.setString(3, clevel);
            pstmt.executeUpdate();
        }
        System.out.format("Inserted (%s, %s, %s)\n", cid, cname, clevel);
    }

    private String getProcedureArgs(final String message) {
        System.out.print(message + ": ");
        return scanner.nextLine();
    }

    private void compensateTranslators() throws SQLException {
        final String aname = getProcedureArgs("Enter aname");

        try(final CallableStatement pstmt = connection.prepareCall("{call book_package.compensate_translators(?)}")) {
            pstmt.setString(1, aname);
            pstmt.executeUpdate();
        }
        System.out.println("Updated Translator Salaries based on " + aname);
    }

    private void display() throws SQLException {
        try(final Statement statement = connection.createStatement()) {
            displayCustomers(statement);
            displayTranslators(statement);
        }
    }

    private void displayCustomers(final Statement statement) throws SQLException {
        System.out.println("Customer ID, Customer Name, Customer orders, Customer level");
        try(final ResultSet rset = statement.executeQuery("select * from customer")) {
            while (rset.next()) {
                System.out.format("%1$-13s%2$-15s%3$-15s%4$-10s\n",
                        rset.getInt(1), rset.getString(2), rset.getInt(3), rset.getString(4));
            }
        }
    }

    private void displayTranslators(final Statement statement) throws SQLException {
        System.out.println("Translator ID, Translator Name, Translator Salary");
        try(final ResultSet rset = statement.executeQuery("select * from translator")) {
            while (rset.next()) {
                System.out.format("%1$-13s%2$-15s%3$-15s\n",
                        rset.getInt(1), rset.getString(2), rset.getInt(3));
            }
        }
    }

    private enum Option {
        INSERT(1), COMPENSATE(2), DISPLAY(3), QUIT(4);
        final int value;
        Option(final int value) {
            this.value = value;
        }
    }
}
