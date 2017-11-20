import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.InputMismatchException;
import java.util.Scanner;

public class OptionManager {

    enum Person {
        Employee(0),
        Volunteer(1),
        Client(2),
        Donor(3);

        int num;

        Person(int num) {
            this.num = num;
        }
    }

    private final Connection connection;
    private Scanner scanner;
    private SimpleDateFormat dateFormat = new SimpleDateFormat( "MM/dd/yyyy" );  // United States style of format.

    public OptionManager(final Connection connection) throws SQLException {
        this.connection = connection;
    }

    public void handleOptions() throws SQLException {
        try(final Scanner scanner = new Scanner(System.in)) {
            this.scanner = scanner;
            int input = getUserOption();
            while(input != OptionManager.Option.QUIT.value) {
                handleInput(input);
                input = getUserOption();
            }
            System.out.println("Thank you for using PAN");
        }
    }

    private int getUserOption() {
        int numOptions = Option.values().length;
        try {
            System.out.print("Enter a number (1-" + numOptions + ") " + numOptions + " to quit: ");
            final int input = scanner.nextInt();
            scanner.nextLine();
            return input;
        } catch(InputMismatchException i) {
            scanner.nextLine();
            return -1;
        }
    }

    private void handleInput(final int input) throws SQLException {
        if(input == OptionManager.Option.INSERT_TEAM.value) { // 1
            System.out.println("Inserting team");
            insertTeam();
        } else if(input == OptionManager.Option.INSERT_CLIENT.value) { // 2
            System.out.println("Inserting client");
            final String clientSSN = insertPersonIfWant(Person.Client);
        } else if(input == OptionManager.Option.INSERT_VOLUNTEER.value) { // 3
            System.out.println("Inserting volunteer");
        } else if(input == OptionManager.Option.INSERT_HOURS_WORKED.value) { // 4
            System.out.println("Inserting hours worked");
        } else if(input == OptionManager.Option.INSERT_EMPLOYEE.value) { // 5
            System.out.println("Inserting employee");
        } else if(input == OptionManager.Option.INSERT_EXPENSE.value) { // 6
            System.out.println("Inserting expense");
        } else if(input == OptionManager.Option.INSERT_ORGANIZATION_WITH_TEAMS.value) { // 7
            System.out.println("Inserting organization with teams");
        } else if(input == OptionManager.Option.INSERT_DONOR.value) { // 8
            System.out.println("Inserting donor");
        } else if(input == OptionManager.Option.INSERT_ORGANIZATION_WITH_DONATIONS.value) { // 9
            System.out.println("Inserting Organization with Donations");
        } else if(input == OptionManager.Option.RETRIEVE_NAME_AND_PHONE.value) { // 10
            System.out.println("Retrieving Name and Phone");
        } else if(input == OptionManager.Option.RETRIEVE_TOTAL_EXPENSE_AMOUNT.value) { // 11
            System.out.println("Retrieving Total Expense Amount");
        } else if(input == OptionManager.Option.RETRIEVE_VOLUNTEERS_SERVING_CLIENT.value) { // 12
            System.out.println("Retrieving Volunteers Serving Client");
        } else if(input == OptionManager.Option.RETRIEVE_CLIENT_WITH_ORG_SPONSOR.value) { // 13
            System.out.println("Retrieving Clients with Org Sponsors");
        } else if(input == OptionManager.Option.RETRIEVE_EMPLOYEE_DONORS.value) { // 14
            System.out.println("Retrieving Employee Donors");
        } else if(input == OptionManager.Option.RETRIEVE_VOLUNTEER_RANGE.value) { // 15
            System.out.println("Retrieving Volunteer Range");
        } else if(input == OptionManager.Option.INCREASE_EMPLOYEE_SALARY.value) { // 16
            System.out.println("Increasing Employee Salary");
        } else if(input == OptionManager.Option.DELETE_CLIENTS.value) { // 17
            System.out.println("Deleting Clients");
        } else if(input == OptionManager.Option.IMPORT_FROM_FILE.value) { // 18
            System.out.println("Importing File");
        } else if(input == OptionManager.Option.EXPORT_MAILING_LIST.value) { // 19
            System.out.println("Export to File");
        } else {
            System.out.println("Invalid input.");
        }
    }

    private String getStringInput(final String message) {
        System.out.print(message + ": ");
        return scanner.nextLine();
    }

    private boolean getBoolInput(final String message) {
        final String input = getStringInput(message + " - [y/n]");
        return input.equalsIgnoreCase("y");
    }

    private long longProcedureArgs(final String message) {
        final String input = getStringInput(message + " (number)");
        return Long.parseLong(input);
    }

    private java.sql.Date dateProcedureArgs(final String message) {
        try {
            final String input = getStringInput(message + " - " + dateFormat.toPattern());
            return new java.sql.Date(dateFormat.parse(input).getTime());
        } catch(ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int intProcedureArgs(final String message) {
        final String input = getStringInput(message + " (number)");
        return Integer.parseInt(input);
    }

    private int boolProcedureArgs(final String message) {
        return getBoolInput(message) ? 1 : 0;
    }

    // Query 1
    private void insertTeam() throws SQLException {
        final String reportSSN = insertPersonIfWant(Person.Employee);
        final String leaderSSN = insertPersonIfWant(Person.Volunteer);
        final String teamName = getStringInput("Enter team name");

        try(final CallableStatement pstmt = connection.prepareCall("{call insertions.insert_team(?,?,?,?,?)}")) {
            pstmt.setString(1, teamName);
            pstmt.setString(2, reportSSN);
            pstmt.setString(3, leaderSSN);
            pstmt.executeUpdate();
        }
        System.out.format("Inserted (%s, %s, %s)\n\n", teamName, reportSSN, leaderSSN);
    }

    private String insertPersonIfWant(Person type) throws SQLException {
        if(!getBoolInput("Insert Person Info?")) {
            return getStringInput("Then give me a ssn");
        }

        final String emailAddress = insertContactInformation();
        return insertPerson(emailAddress, type);
    }

    private String insertContactInformation() throws SQLException {
        System.out.println("Inserting contact information");
        final String emailAddress = getStringInput("Enter email address");
        final String mailingAddress = getStringInput("Enter mailing address");
        final long homeNumber = longProcedureArgs("Enter home number");
        final long mobileNumber = longProcedureArgs("Enter mobile number");
        final long cellNumber = longProcedureArgs("Enter cell number");

        try(final CallableStatement pstmt = connection.prepareCall("{call insertions.insert_contact_info(?,?,?,?,?)}")) {
            pstmt.setString(1, emailAddress);
            pstmt.setString(2, mailingAddress);
            pstmt.setLong(3, homeNumber);
            pstmt.setLong(4, mobileNumber);
            pstmt.setLong(5, cellNumber);
            pstmt.executeUpdate();
        }
        System.out.format("Inserted (%s, %s, %d, %d, %d)\n\n", emailAddress, mailingAddress, homeNumber, mobileNumber, cellNumber);
        return emailAddress;
    }

    private String insertPerson(final String emailAddress, final Person type) throws SQLException {
        System.out.println("Inserting person");
        final String ssn = getStringInput("Enter ssn");
        final String fullName = getStringInput("Enter full name");
        final java.sql.Date dob = dateProcedureArgs("Enter dob");
        final String race = getStringInput("Enter race");
        final int gender = boolProcedureArgs("Is Male?");
        final String profession = getStringInput("Enter profession");
        final int shouldEmail = boolProcedureArgs("Should email?");

        try(final CallableStatement pstmt = connection.prepareCall("{call insertions.insert_person(?,?,?,?,?,?,?,?)}")) {
            pstmt.setString(1, ssn);
            pstmt.setString(2, emailAddress);
            pstmt.setString(3, fullName);
            pstmt.setDate(4, dob);
            pstmt.setString(5, race);
            pstmt.setInt(6, gender);
            pstmt.setString(7, profession);
            pstmt.setInt(8, shouldEmail);
            pstmt.executeUpdate();
        }
        System.out.format("Inserted (%s, %s, %s, %s, %s, %d, %s, %d)\n\n", emailAddress, ssn, fullName, dob.toString(), race, gender, profession, shouldEmail);
        insertEmergencyContact(ssn, emailAddress);
        insertPersonType(ssn,  type);
        return ssn;
    }

    private void insertEmergencyContact(final String ssn, final String emailAddress) throws SQLException {
        System.out.println("Inserting emergency contact");
        final String contactName = getStringInput("Enter contact name");
        final String relationship = getStringInput("Enter relationship");

        try(final CallableStatement pstmt = connection.prepareCall("{call insertions.insert_emergency_contact(?,?,?,?)}")) {
            pstmt.setString(1, ssn);
            pstmt.setString(2, contactName);
            pstmt.setString(3, relationship);
            pstmt.setString(4, emailAddress);
            pstmt.executeUpdate();
        }
        System.out.format("Inserted (%s, %s, %s, %s)\n\n", ssn, contactName, relationship, emailAddress);
        insertContactInformation();
    }

    private void insertPersonType(final String ssn, final Person type) throws SQLException {
        if (type == Person.Employee) {
            insertEmployee(ssn);
        } else if (type == Person.Volunteer) {
            insertVolunteer(ssn);
        } else if (type == Person.Client) {
            insertClient(ssn);
        } else if (type == Person.Donor) {

        }
    }

    private void insertEmployee(final String ssn) throws SQLException {
        System.out.println("Entering employee");
        final int salary = intProcedureArgs("Enter salary");
        final String maritalStatus = getStringInput("Enter marital status");
        final java.sql.Date hireDate = dateProcedureArgs("Enter hire date");
        try(final CallableStatement pstmt = connection.prepareCall("{call insertions.insert_reported_employee(?,?,?,?)}")) {
            pstmt.setString(1, ssn);
            pstmt.setInt(2, salary);
            pstmt.setString(3, maritalStatus);
            pstmt.setDate(4, hireDate);
            pstmt.executeUpdate();
        }
        System.out.format("Inserted (%s, %d, %s, %s)\n\n", ssn, salary, maritalStatus, hireDate.toString());
    }

    private void insertVolunteer(final String ssn) throws SQLException {
        System.out.println("Entering volunteer");
        final Date joinDate = dateProcedureArgs("Enter date joined");
        final Date dateTrainedCourse = dateProcedureArgs("Enter date trained course");
        final String locationTrainedCourse = getStringInput("Enter location trained course");
        try(final CallableStatement pstmt = connection.prepareCall("{call insertions.insert_volunteer(?,?,?,?)}")) {
            pstmt.setString(1, ssn);
            pstmt.setDate(2, joinDate);
            pstmt.setDate(3, dateTrainedCourse);
            pstmt.setString(4, locationTrainedCourse);
            pstmt.executeUpdate();
        }
        System.out.format("Inserted (%s, %s, %s, %s)\n\n", ssn, joinDate.toString(), dateTrainedCourse.toString(), locationTrainedCourse);
    }

    private void insertClient(final String ssn) throws SQLException {
        System.out.println("Entering Client");
        final String doctorName = getStringInput("Enter doctor name");
        final long doctorPhone = longProcedureArgs("Enter doctor phone");
        final String attorneyName = getStringInput("Enter attorney name");
        final long attorneyPhone = longProcedureArgs("Enter attorney phone");

        try(final CallableStatement pstmt = connection.prepareCall("{call insertions.insert_client(?,?,?,?,?)}")) {
            pstmt.setString(1, ssn);
            pstmt.setString(2, doctorName);
            pstmt.setLong(3, doctorPhone);
            pstmt.setString(4, attorneyName);
            pstmt.setLong(5, attorneyPhone);
            pstmt.executeUpdate();
        }
        System.out.format("Inserted (%s, %s, %s, %s, %s)\n\n", ssn, doctorName, doctorPhone, attorneyName, attorneyPhone);
        insertPolicy(ssn);
        insertClientNeed(ssn);
        associateWithManyTeams(ssn);
    }

    private void insertPolicy(final String ssn) throws SQLException {
        System.out.println("Entering Policy");
        final int policyID = intProcedureArgs("Enter policy id");
        final int providerID = intProcedureArgs("Enter provider id");
        final String providerAddress = getStringInput("Enter provider address");
        final String policyType = getStringInput("Enter policy type");

        try(final CallableStatement pstmt = connection.prepareCall("{call insertions.insert_policy(?,?,?,?,?)}")) {
            pstmt.setInt(1, policyID);
            pstmt.setString(2, ssn);
            pstmt.setInt(3, providerID);
            pstmt.setString(4, providerAddress);
            pstmt.setString(5, policyType);
            pstmt.executeUpdate();
        }
        System.out.format("Inserted (%s, %s, %s, %s, %s, %s)\n\n", ssn, policyID, ssn, providerID, providerAddress, policyType);
    }

    private void insertClientNeed(final String ssn) throws SQLException {
        System.out.println("Entering Client Need");
        final String needType = getStringInput("Enter need type");
        final int importance = intProcedureArgs("Enter importance");

        try(final CallableStatement pstmt = connection.prepareCall("{call insertions.insert_client_need(?,?,?)}")) {
            pstmt.setString(1, ssn);
            pstmt.setString(2, needType);
            pstmt.setInt(3, importance);
            pstmt.executeUpdate();
        }
        System.out.format("Inserted (%s, %s, %s)\n\n", ssn, needType, importance);
    }

    private void associateWithManyTeams(final String ssn) throws SQLException {
        System.out.println("Associating with many teams");
        do {
            final String teamName = getStringInput("Enter team name");
            final int isActive = boolProcedureArgs("Is active");

            try(final CallableStatement pstmt = connection.prepareCall("{call insertions.associate_client_to_team(?,?,?)}")) {
                pstmt.setString(1, ssn);
                pstmt.setString(2, teamName);
                pstmt.setInt(3, isActive);
                pstmt.executeUpdate();
            }
            System.out.format("Inserted (%s, %s, %s)\n\n", ssn, teamName, isActive);
        } while(getBoolInput("Associate with another team?"));
    }

    private void compensateTranslators() throws SQLException {
        final String aname = getStringInput("Enter aname");

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
        INSERT_TEAM(1),
        INSERT_CLIENT(2),
        INSERT_VOLUNTEER(3),
        INSERT_HOURS_WORKED(4),
        INSERT_EMPLOYEE(5),
        INSERT_EXPENSE(6),
        INSERT_ORGANIZATION_WITH_TEAMS(7),
        INSERT_DONOR(8),
        INSERT_ORGANIZATION_WITH_DONATIONS(9),
        RETRIEVE_NAME_AND_PHONE(10),
        RETRIEVE_TOTAL_EXPENSE_AMOUNT(11),
        RETRIEVE_VOLUNTEERS_SERVING_CLIENT(12),
        RETRIEVE_CLIENT_WITH_ORG_SPONSOR(13),
        RETRIEVE_EMPLOYEE_DONORS(14),
        RETRIEVE_VOLUNTEER_RANGE(15),
        INCREASE_EMPLOYEE_SALARY(16),
        DELETE_CLIENTS(17),
        IMPORT_FROM_FILE(18),
        EXPORT_MAILING_LIST(19),
        QUIT(20);

        final int value;
        Option(final int value) {
            this.value = value;
        }
    }
}
