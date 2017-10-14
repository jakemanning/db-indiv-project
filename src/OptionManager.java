import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.InputMismatchException;
import java.util.Scanner;

public class OptionManager {

    private final Statement statement;

    public OptionManager(final Statement statement) throws SQLException {
        this.statement = statement;
    }

    public void handleOptions() throws SQLException {
        createTable();
        Scanner scanner = new Scanner(System.in);
        int input = getUserInput(scanner);
        while(input != Option.QUIT.value) {
            handleInput(input);
            input = getUserInput(scanner);
        }

        System.out.println("Thank you for using BoomerSQLer");
        dropTable();
        scanner.close();
    }

    private int getUserInput(final Scanner scanner) {
        try {
            System.out.print("Enter a number (1-4): ");
            int input = scanner.nextInt();
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
            displayCustomers();
        } else {
            System.out.println("Invalid input.");
        }
    }

    private void createTable() throws SQLException {
        String createTable = "create table customer (" +
                "cid NUMBER," +
                "cname VARCHAR(30)," +
                "number_of_orders NUMBER," +
                "PRIMARY KEY (cid))";
        statement.executeUpdate(createTable);
    }

    private void insertCustomer() throws SQLException {
        // TODO
        //         Assume that the value of number_of_orders is not known, and will be estimated using the current information in
        //        the database. The number_of_orders value should be set to the rounded average of the number_of_orders
        //        values for all the other customers with the same level. If there is no such customer, the new customer’s
        //        number_of_orders should be set to the rounded average of number_of_orders of all the existing
        //        customers
        System.out.println("Inserting Customer(s)");
        String insertCustomer1 = "insert into customer values (100, 'customer 1', 2)";
        String insertCustomer2 = "insert into customer values (200, 'customer 2', 5)";
        statement.executeUpdate(insertCustomer1);
        statement.executeUpdate(insertCustomer2);
    }

    private void compensateTranslators() throws SQLException {
        System.out.println("Compensating Translators");
    }

    private void displayCustomers() throws SQLException {
        System.out.println("Displaying Customers");

        ResultSet rset = statement.executeQuery("select * from customer");
        System.out.println("Customer ID, Customer Name, Customer orders");
        while(rset.next()) {
            System.out.format("%1$-13s%2$-15s%3$-15s\n",
                    rset.getString(1), rset.getString(2), rset.getString(3));
        }
    }

    private void dropTable() throws SQLException {
        statement.executeUpdate("drop table customer");
    }

    private enum Option {
        INSERT(1), COMPENSATE(2), DISPLAY(3), QUIT(4);
        final int value;
        Option(final int value) {
            this.value = value;
        }
    }
}
