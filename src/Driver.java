import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Aaron on 10/13/2017.
 */
public class Driver {
    public static void main(String[] args) {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch(Exception x) {
            System.err.println("Unable to load the driver class!");
        }

        final String url = "jdbc:oracle:thin:@//oracle.cs.ou.edu:1521/pdborcl.cs.ou.edu";
        Connection dbConnection = createConnection(url);
        Statement statement = null;
        if(dbConnection != null) {
            try {
                statement = dbConnection.createStatement();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            return;
        }

        try {
            OptionManager manager = new OptionManager(statement);
            manager.handleOptions();
            dbConnection.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static Connection createConnection(final String url) {
        // Pretend the DB works
        try {
            Credentials credentials = new Credentials();
            System.out.println("Connecting...");
            Connection dbConnection = DriverManager.getConnection(url, credentials.username, credentials.password);
            System.out.println("Connected");
            return dbConnection;
        } catch(SQLException|IOException x) {
            System.err.println("Couldn't connect");
            x.printStackTrace();
            return null;
        }
    }
}
