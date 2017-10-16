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
        final Connection dbConnection = createConnection(url);
        if(dbConnection == null) {
            return;
        }

        try {
            final OptionManager manager = new OptionManager(dbConnection);
            manager.handleOptions();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection(dbConnection);
        }
    }

    private static void loadJDBC() {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch(Exception x) {
            System.err.println("Unable to load the driver class!");
        }
    }

    private static Connection createConnection(final String url) {
        // Pretend the DB works
        try {
            final Credentials credentials = new Credentials();
            System.out.println("Connecting...");
            final Connection dbConnection = DriverManager.getConnection(url, credentials.username, credentials.password);
            System.out.println("Connected");
            return dbConnection;
        } catch(SQLException s) {
            System.err.println(s.getMessage());
            return null;
        } catch(IOException i) {
            i.printStackTrace();
            return null;
        }
    }

    private static void closeConnection(final Connection connection) {
        try {
            if(connection != null) {
                connection.close();
            }
        } catch(SQLException s) {
            System.err.println(s.getMessage());
        }
    }
}
