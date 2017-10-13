import java.util.Scanner;
import java.sql.*;

/**
 * Created by Aaron on 10/13/2017.
 */
public class Driver {
    public static void main(String[] args){
        Connection dbConnection = null;
        Statement stmt = null;
        String user, password;
        String url = "jdbc:oracle:thin:@//oracle.cs.ou.edu:1521/pdborcl.cs.ou.edu";
        user = "wils0003";
        password = "ZFwc7Ab2";
        //Use to enter custom stuff
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Enter a username: ");
//        user = scanner.nextLine();
//        System.out.println("Enter a password: ");
//        password = scanner.nextLine();


        try {
            Class.forName("oracle.jdbc.OracleDriver");

        }catch(Exception x){
            System.out.println("Unable to load the driver class!");
        }

        dbConnection = createConnection(url, user, password);

        if(dbConnection != null) {
            try {
                stmt = dbConnection.createStatement();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        // create table customer
        String sqlCreate, sqlInsert1, sqlInsert2 ;
        sqlCreate = "create table customer (" +
                "cid NUMBER, " +
                "cname VARCHAR(30), " +
//                //TODO
//         Assume that the value of number_of_orders is not known, and will be estimated using the current information in
//        the database. The number_of_orders value should be set to the rounded average of the number_of_orders
//        values for all the other customers with the same level. If there is no such customer, the new customer’s
//        number_of_orders should be set to the rounded average of number_of_orders of all the existing
//        customers
                "number_of_orders NUMBER" +
                "PRIMARY KEY (cid))";

        sqlInsert1 = "insert into customer values (100, 'customer 1', 2)";
        sqlInsert2 = "insert into customer values (200, 'customer 2', 5)";

        try {
            //Create db table
            stmt.executeUpdate(sqlCreate);

            //Insert data
            stmt.executeUpdate(sqlInsert1);
            stmt.executeUpdate(sqlInsert2);

            // display the IDs and names column of all students using the JDBC resultSet
            ResultSet rset = stmt.executeQuery("select * from customer");
            System.out.println("Customer ID, Customer Name, Customer orders");
            while (rset.next()) {
                System.out.println(rset.getString(1) + " " + rset.getString(2));
            }



            dbConnection.close();
        }catch(Exception e){
            System.out.println (e.getMessage());
            System.out.println ("Exception occurred in executing the statement");
        }
    }

    public static Connection createConnection(String url, String user, String password){
        Connection dbConnection;
        //Pretend the DB works
        try{
            dbConnection=DriverManager.getConnection
                    (url,user,password);
            System.out.println("connected");
        }
        catch( SQLException x ){
            System.out.println("Can't connect");
            return null;
        }
        return dbConnection;
    }
}
