import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by The Boomer Team™️ on 10/13/2017.
 */
public class Driver {
    public static void main(String[] args) {
        loadJDBC();
        final String url = "jdbc:oracle:thin:@//oracle.cs.ou.edu:1521/pdborcl.cs.ou.edu";
        System.out.println("Connecting...");
        final Credentials credentials;
        try {
            credentials = new Credentials();
        } catch(IOException e) {
            e.printStackTrace();
            return;
        }

        try(Connection dbConnection = DriverManager.getConnection(url, credentials.username, credentials.password)) {
            System.out.println("Connected");
            final OptionManager manager = new OptionManager(dbConnection);
            manager.handleOptions();
        } catch(SQLException s) {
            System.err.println(s.getMessage());
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
