import java.io.IOException;
import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Created by Jake Manning
 */
public class Driver {
    public static void main(String[] args) throws IOException {
        loadJDBC();
        final String url = "jdbc:oracle:thin:@//oracle.cs.ou.edu:1521/pdborcl.cs.ou.edu";
        System.out.println("Connecting...");
        Credentials credentials = new Credentials();
        try(Connection dbConnection = DriverManager.getConnection(url, "mann7942", "FYkk6Nb5")) {
            System.out.println("Connected\n");
            System.out.println("Welcome to the PAN Client and Donor Database System");
            final OptionManager manager = new OptionManager(dbConnection);
            manager.handleOptions();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadJDBC() {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch(Exception x) {
            System.err.println("Unable to load the driver class!");
        }
    }
}
