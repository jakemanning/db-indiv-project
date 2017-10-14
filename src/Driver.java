import java.sql.*;

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
        Statement stmt = null;
        if(dbConnection != null) {
            try {
                stmt = dbConnection.createStatement();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            return;
        }

        String createTable = "create table customer (" +
                "cid NUMBER," +
                "cname VARCHAR(30)," +
                "number_of_orders NUMBER," +
                "PRIMARY KEY (cid))";

        // TODO
        //         Assume that the value of number_of_orders is not known, and will be estimated using the current information in
        //        the database. The number_of_orders value should be set to the rounded average of the number_of_orders
        //        values for all the other customers with the same level. If there is no such customer, the new customer’s
        //        number_of_orders should be set to the rounded average of number_of_orders of all the existing
        //        customers
        String insertCustomer1 = "insert into customer values (100, 'customer 1', 2)";
        String insertCustomer2 = "insert into customer values (200, 'customer 2', 5)";
        String dropTable = "drop table customer";

        try {
            stmt.executeUpdate(createTable);
            stmt.executeUpdate(insertCustomer1);
            stmt.executeUpdate(insertCustomer2);

            // Display the IDs and names column of all students using the JDBC resultSet
            ResultSet rset = stmt.executeQuery("select * from customer");
            System.out.println("Customer ID, Customer Name, Customer orders");
            while(rset.next()) {
                System.out.format("%1$-13s%2$-15s%3$-15s\n",
                        rset.getString(1), rset.getString(2), rset.getString(3));
            }

            stmt.executeUpdate(dropTable);
            dbConnection.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static Connection createConnection(String url){
        // Pretend the DB works
        try {
            Credentials credentials = new Credentials();
            System.out.println("Connecting...");
            Connection dbConnection = DriverManager.getConnection(url, credentials.username, credentials.password);
            System.out.println("Connected");
            return dbConnection;
        } catch(SQLException x) {
            System.err.println("Couldn't connect");
            return null;
        }
    }
}
