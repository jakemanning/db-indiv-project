import oracle.jdbc.OracleTypes;

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
            System.out.print("Enter a number (1-" + numOptions + ") " + (numOptions - 1) + " to quit, " + (numOptions) + " to display options: ");
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
            associateWithManyTeams(clientSSN, "associate_client_to_team");
        } else if(input == OptionManager.Option.INSERT_VOLUNTEER.value) { // 3
            System.out.println("Inserting volunteer");
            final String volunteerSSN = insertPersonIfWant(Person.Volunteer);
            associateWithManyTeams(volunteerSSN, "associate_volunteer_to_team");
        } else if(input == OptionManager.Option.INSERT_HOURS_WORKED.value) { // 4
            System.out.println("Inserting hours worked");
            insertHoursWorked();
        } else if(input == OptionManager.Option.INSERT_EMPLOYEE.value) { // 5
            System.out.println("Inserting employee");
            final String employeeSSN = insertPersonIfWant(Person.Employee);
            associateWithManyTeamsLess(employeeSSN, "associate_employee_to_team");
        } else if(input == OptionManager.Option.INSERT_EXPENSE.value) { // 6
            System.out.println("Inserting expense");
            insertExpense();
        } else if(input == OptionManager.Option.INSERT_ORGANIZATION_WITH_TEAMS.value) { // 7
            System.out.println("Inserting organization with teams");
            final String orgName = insertOrganization();
            associateWithManyTeamsLess(orgName, "associate_org_to_team");
        } else if(input == OptionManager.Option.INSERT_DONOR.value) { // 8
            System.out.println("Inserting donor");
            String donorSSN = insertPersonIfWant(Person.Donor);
            insertDonor(donorSSN);
        } else if(input == OptionManager.Option.INSERT_ORGANIZATION_WITH_DONATIONS.value) { // 9
            System.out.println("Inserting Organization with Donations");
            final String orgName = insertOrganization();
            insertOrganizationDonation(orgName);
        } else if(input == OptionManager.Option.RETRIEVE_NAME_AND_PHONE.value) { // 10
            System.out.println("Retrieving Name and Phone");
            displayNameAndPhone();
        } else if(input == OptionManager.Option.RETRIEVE_TOTAL_EXPENSE_AMOUNT.value) { // 11
            System.out.println("Retrieving Total Expense Amount");
            displayExpenseAmount();
        } else if(input == OptionManager.Option.RETRIEVE_VOLUNTEERS_SERVING_CLIENT.value) { // 12
            System.out.println("Retrieving Volunteers Serving Client");
            displayVolunteerMemberList();
        } else if(input == OptionManager.Option.RETRIEVE_CLIENT_WITH_ORG_SPONSOR.value) { // 13
            System.out.println("Retrieving Clients with Org Sponsors");
            displayClientInfoForOrgs();
        } else if(input == OptionManager.Option.RETRIEVE_EMPLOYEE_DONORS.value) { // 14
            System.out.println("Retrieving Employee Donors");
            displayEmployeeDonors();
        } else if(input == OptionManager.Option.RETRIEVE_VOLUNTEER_RANGE.value) { // 15
            System.out.println("Retrieving Volunteer Hours Range");
            displayVolunteerHours();
        } else if(input == OptionManager.Option.INCREASE_EMPLOYEE_SALARY.value) { // 16
            System.out.println("Increasing Employee Salary");
            increaseEmployeeSalaries();
        } else if(input == OptionManager.Option.DELETE_CLIENTS.value) { // 17
            System.out.println("Deleting Clients");
            deleteHealthClients();
        } else if(input == OptionManager.Option.IMPORT_FROM_FILE.value) { // 18
            System.out.println("Importing File");
        } else if(input == OptionManager.Option.EXPORT_MAILING_LIST.value) { // 19
            System.out.println("Export to File");
        } else if(input == OptionManager.Option.DISPLAY_OPTIONS.value) {
            displayOptions();
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

    private void insertTeam() throws SQLException {
        final String reportSSN = insertPersonIfWant(Person.Employee);
        final String leaderSSN = insertPersonIfWant(Person.Volunteer);
        final String teamName = getStringInput("Enter team name");

        try(final CallableStatement pstmt = connection.prepareCall("{call insertions.insert_team(?,?,?)}")) {
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
            // No action necessary
        }
    }

    private void insertEmployee(final String ssn) throws SQLException {
        System.out.println("Entering employee");
        final int salary = intProcedureArgs("Enter salary");
        final String maritalStatus = getStringInput("Enter marital status");
        final java.sql.Date hireDate = dateProcedureArgs("Enter hire date");

        try(final CallableStatement pstmt = connection.prepareCall("{call insertions.insert_employee(?,?,?,?)}")) {
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
    }

    private void insertDonor(final String ssn) throws SQLException {
        System.out.println("Entering donor");
        final java.sql.Date dateDonated = dateProcedureArgs("Enter date donated");
        final int amount = intProcedureArgs("Enter amount");
        final String type = getStringInput("Enter donation type");
        final String campaignName = getStringInput("Enter campaign name");
        final int isAnonymous = boolProcedureArgs("Is anonymous?");

        if(getBoolInput("Pay with credit?")) {
            System.out.println("Entering Credit Payment");

            final String creditCard = getStringInput("Enter credit card number");
            final String cardType = getStringInput("Enter card type");
            final java.sql.Date expirationDate = dateProcedureArgs("Enter expirationDate");
            try(final CallableStatement pstmt = connection.prepareCall("{call insertions.insert_indiv_donation_credit(?,?,?,?,?,?,?,?,?)}")) {
                pstmt.setString(1, ssn);
                pstmt.setDate(2, dateDonated);
                pstmt.setInt(3, amount);
                pstmt.setString(4, type);
                pstmt.setString(5, campaignName);
                pstmt.setInt(6, isAnonymous);
                pstmt.setString(7, creditCard);
                pstmt.setString(8, cardType);
                pstmt.setDate(9, expirationDate);
                pstmt.executeUpdate();
            }
            System.out.format("Inserted (%s, %s, %s, %s, %s, %s, %s, %s, %s)\n\n", ssn, dateDonated.toString(), amount, type, campaignName, isAnonymous, creditCard, cardType, expirationDate.toString());

        } else {
            System.out.println("Entering Check Payment");

            final String check = getStringInput("Enter check number");

            try(final CallableStatement pstmt = connection.prepareCall("{call insertions.insert_indiv_donation_check(?,?,?,?,?,?,?)}")) {
                pstmt.setString(1, ssn);
                pstmt.setDate(2, dateDonated);
                pstmt.setInt(3, amount);
                pstmt.setString(4, type);
                pstmt.setString(5, campaignName);
                pstmt.setInt(6, isAnonymous);
                pstmt.setString(7, check);
                pstmt.executeUpdate();
            }
            System.out.format("Inserted (%s, %s, %s, %s, %s, %s, %s)\n\n", ssn, dateDonated.toString(), amount, type, campaignName, isAnonymous, check);
        }
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

    private void associateWithManyTeams(final String ssn, final String query) throws SQLException {
        System.out.println("Associating with many teams");
        do {
            final String teamName = getStringInput("Enter team name");
            final int isActive = boolProcedureArgs("Is active");

            try(final CallableStatement pstmt = connection.prepareCall("{call insertions." + query + "(?,?,?)}")) {
                pstmt.setString(1, ssn);
                pstmt.setString(2, teamName);
                pstmt.setInt(3, isActive);
                pstmt.executeUpdate();
            }
            System.out.format("Inserted (%s, %s, %s)\n\n", ssn, teamName, isActive);
        } while(getBoolInput("Associate with another team?"));
    }

    private void associateWithManyTeamsLess(final String ssn, final String query) throws SQLException {
        System.out.println("Associating with many teams");
        do {
            final String teamName = getStringInput("Enter team name");

            try(final CallableStatement pstmt = connection.prepareCall("{call insertions." + query + "(?,?)}")) {
                pstmt.setString(1, ssn);
                pstmt.setString(2, teamName);
                pstmt.executeUpdate();
            }
            System.out.format("Inserted (%s, %s)\n\n", ssn, teamName);
        } while(getBoolInput("Associate with another team?"));
    }

    private void insertHoursWorked() throws SQLException {
        System.out.println("Entering hours worked");
        final String ssn = getStringInput("Enter ssn");
        final String teamName = getStringInput("Enter team name");
        final int workMonth = intProcedureArgs("Enter work month (1-12)");
        final int hoursWorked = intProcedureArgs("Enter hours worked");

        try(final CallableStatement pstmt = connection.prepareCall("{call insertions.insert_hours_worked(?,?,?,?)}")) {
            pstmt.setString(1, ssn);
            pstmt.setString(2, teamName);
            pstmt.setInt(3, workMonth);
            pstmt.setInt(4, hoursWorked);
            pstmt.executeUpdate();
        }
        System.out.format("Inserted (%s, %s, %s)\n\n", ssn, teamName, workMonth, hoursWorked);
    }

    private void insertExpense() throws SQLException {
        System.out.println("Entering expense");
        final String ssn = getStringInput("Enter employee ssn");
        final java.sql.Date dateEntered = dateProcedureArgs("Enter date entered");
        final int amount = intProcedureArgs("Enter amount");
        final String description = getStringInput("Enter expense description");

        try(final CallableStatement pstmt = connection.prepareCall("{call insertions.insert_expense(?,?,?,?)}")) {
            pstmt.setString(1, ssn);
            pstmt.setDate(2, dateEntered);
            pstmt.setInt(3, amount);
            pstmt.setString(4, description);
            pstmt.executeUpdate();
        }
        System.out.format("Inserted (%s, %s, %s, %s)\n\n", ssn, dateEntered.toString(), amount, description);
    }

    private String insertOrganization() throws SQLException {
        System.out.println("Entering organization");
        final String orgName = getStringInput("Enter org name");
        final String mailingAddress = getStringInput("Enter mailing address");
        final long phoneNumber = longProcedureArgs("Enter phone number");
        final String contactPersonName = getStringInput("Enter contact person name");
        final int isAnonymous = boolProcedureArgs("Is anonymous?");

        try(final CallableStatement pstmt = connection.prepareCall("{call insertions.insert_organization(?,?,?,?,?)}")) {
            pstmt.setString(1, orgName);
            pstmt.setString(2, mailingAddress);
            pstmt.setLong(3, phoneNumber);
            pstmt.setString(4, contactPersonName);
            pstmt.setInt(5, isAnonymous);
            pstmt.executeUpdate();
        }
        System.out.format("Inserted (%s, %s, %s, %s, %s)\n\n", orgName, mailingAddress, phoneNumber, contactPersonName, isAnonymous);
        if(getBoolInput("Is business?")) {
            insertBusiness(orgName);
        } else if(getBoolInput("Is church?")) {
            insertChurch(orgName);
        }
        return orgName;
    }

    private void insertBusiness(final String orgName) throws SQLException {
        System.out.println("Entering business");
        final String businessType = getStringInput("Enter business type");
        final int businessSize = intProcedureArgs("Enter business size");
        final String website = getStringInput("Enter website");

        try(final CallableStatement pstmt = connection.prepareCall("{call insertions.insert_business(?,?,?,?)}")) {
            pstmt.setString(1, orgName);
            pstmt.setString(2, businessType);
            pstmt.setInt(3, businessSize);
            pstmt.setString(4, website);
            pstmt.executeUpdate();
        }
        System.out.format("Inserted (%s, %s, %s, %s)\n\n", orgName, businessType, businessSize, website);
    }

    private void insertChurch(final String orgName) throws SQLException {
        System.out.println("Entering church");
        final String religiousAffiliation = getStringInput("Enter religious affiliation");

        try(final CallableStatement pstmt = connection.prepareCall("{call insertions.insert_church(?,?)}")) {
            pstmt.setString(1, orgName);
            pstmt.setString(2, religiousAffiliation);
            pstmt.executeUpdate();
        }
        System.out.format("Inserted (%s, %s)\n\n", orgName, religiousAffiliation);
    }

    private void insertOrganizationDonation(final String orgName) throws SQLException {
        System.out.println("Entering organization donation");
        final java.sql.Date dateDonated = dateProcedureArgs("Enter date donated");
        final int amount = intProcedureArgs("Enter amount");
        final String type = getStringInput("Enter donation type");
        final String campaignName = getStringInput("Enter campaign name");

        if(getBoolInput("Pay with credit?")) {
            System.out.println("Entering Credit Payment");

            final String creditCard = getStringInput("Enter credit card number");
            final String cardType = getStringInput("Enter card type");
            final java.sql.Date expirationDate = dateProcedureArgs("Enter expirationDate");
            try(final CallableStatement pstmt = connection.prepareCall("{call insertions.insert_org_donation_credit(?,?,?,?,?,?,?,?)}")) {
                pstmt.setString(1, orgName);
                pstmt.setDate(2, dateDonated);
                pstmt.setInt(3, amount);
                pstmt.setString(4, type);
                pstmt.setString(5, campaignName);
                pstmt.setString(6, creditCard);
                pstmt.setString(7, cardType);
                pstmt.setDate(8, expirationDate);
                pstmt.executeUpdate();
            }
            System.out.format("Inserted (%s, %s, %s, %s, %s, %s, %s, %s)\n\n", orgName, dateDonated.toString(), amount, type, campaignName, creditCard, cardType, expirationDate.toString());

        } else {
            System.out.println("Entering Check Payment");

            final String check = getStringInput("Enter check number");

            try(final CallableStatement pstmt = connection.prepareCall("{call insertions.insert_org_donation_check(?,?,?,?,?,?)}")) {
                pstmt.setString(1, orgName);
                pstmt.setDate(2, dateDonated);
                pstmt.setInt(3, amount);
                pstmt.setString(4, type);
                pstmt.setString(5, campaignName);
                pstmt.setString(6, check);
                pstmt.executeUpdate();
            }
            System.out.format("Inserted (%s, %s, %s, %s, %s, %s, %s)\n\n", orgName, dateDonated.toString(), amount, type, campaignName, check);
        }
    }

    private void displayNameAndPhone() throws SQLException {
        final String clientSSN = getStringInput("Enter client ssn");
        try(final CallableStatement pstmt = connection.prepareCall("{call retrievals.retrieve_client_info(?,?,?)}")) {
            pstmt.setString(1, clientSSN);
            pstmt.registerOutParameter(2, OracleTypes.VARCHAR);
            pstmt.registerOutParameter(3, OracleTypes.VARCHAR);
            pstmt.executeUpdate();

            final String name = pstmt.getString(2);
            final String phone = pstmt.getString(3);
            System.out.format("Retrieved: %s, %s\n\n", name, phone);
        }
    }

    private void displayExpenseAmount() throws SQLException {
        try(final CallableStatement pstmt = connection.prepareCall("{call retrievals.retrieve_expense_amount(?)}")) {
            pstmt.registerOutParameter(1, OracleTypes.CURSOR);
            pstmt.executeUpdate();

            System.out.println("SSN | total_amount");
            final ResultSet rs = (ResultSet) pstmt.getObject(1);
            while(rs.next()) {
                String ssn = rs.getString("ssn");
                long totalAmount = rs.getLong("total_amount");
                System.out.format("%s, %s\n", ssn, totalAmount);
            }
        }
    }

    private void displayVolunteerMemberList() throws SQLException {
        final String clientSSN = getStringInput("Enter client ssn");
        try(final CallableStatement pstmt = connection.prepareCall("{call retrievals.retrieve_volunteer_member_list(?,?)}")) {
            pstmt.setString(1, clientSSN);
            pstmt.registerOutParameter(2, OracleTypes.CURSOR);
            pstmt.executeUpdate();

            System.out.println("SSN");
            final ResultSet rs = (ResultSet) pstmt.getObject(2);
            while(rs.next()) {
                String ssn = rs.getString("ssn");
                System.out.format("%s\n", ssn);
            }
        }
    }

    private void displayClientInfoForOrgs() throws SQLException {
        try(final CallableStatement pstmt = connection.prepareCall("{call retrievals.retrieve_client_info_for_orgs(?)}")) {
            pstmt.registerOutParameter(1, OracleTypes.CURSOR);
            pstmt.executeUpdate();

            System.out.println("full_name | email_address | mailing_addr | home_number | mobile_number | cell_number");
            final ResultSet rs = (ResultSet) pstmt.getObject(1);
            while(rs.next()) {
                String fullName = rs.getString("full_name");
                String emailAddr = rs.getString("email_addr");
                String mailingAddr = rs.getString("mailing_addr");
                int homeNumber = rs.getInt("home_number");
                int mobileNumber = rs.getInt("mobile_number");
                int cellNumber = rs.getInt("cell_number");
                System.out.format("%s, %s, %s, %s, %s, %s\n", fullName, emailAddr, mailingAddr, homeNumber, mobileNumber, cellNumber);
            }
        }
    }

    private void displayEmployeeDonors() throws SQLException {
        try(final CallableStatement pstmt = connection.prepareCall("{call retrievals.retrieve_donor_emp_info(?)}")) {
            pstmt.registerOutParameter(1, OracleTypes.CURSOR);
            pstmt.executeUpdate();

            System.out.println("full_name | total_amount | is_anonymous");
            final ResultSet rs = (ResultSet) pstmt.getObject(1);
            while(rs.next()) {
                String fullName = rs.getString("full_name");
                long totalAmount = rs.getLong("total_amount");
                boolean isAnonymous = rs.getInt("is_anonymous") == 1;
                System.out.format("%s, %s, %s\n", fullName, totalAmount, isAnonymous);
            }
        }
    }

    private void displayVolunteerHours() throws SQLException {
        try(final CallableStatement pstmt = connection.prepareCall("{call retrievals.retrieve_volunteer_info_hours(?)}")) {
            pstmt.registerOutParameter(1, OracleTypes.CURSOR);
            pstmt.executeUpdate();

            System.out.println("full_name | email_address | mailing_addr | home_number | mobile_number | cell_number");
            final ResultSet rs = (ResultSet) pstmt.getObject(1);
            while(rs.next()) {
                String fullName = rs.getString("full_name");
                String emailAddr = rs.getString("email_addr");
                String mailingAddr = rs.getString("mailing_addr");
                int homeNumber = rs.getInt("home_number");
                int mobileNumber = rs.getInt("mobile_number");
                int cellNumber = rs.getInt("cell_number");
                System.out.format("%s, %s, %s, %s, %s, %s\n", fullName, emailAddr, mailingAddr, homeNumber, mobileNumber, cellNumber);
            }
        }
    }

    private void increaseEmployeeSalaries() throws SQLException {
        try(final CallableStatement pstmt = connection.prepareCall("{call modifications.increase_employee_salaries}")) {
            pstmt.executeUpdate();
        }
        System.out.println("Increased salaries");
    }

    private void deleteHealthClients() throws SQLException {
        try(final CallableStatement pstmt = connection.prepareCall("{call modifications.delete_health_clients}")) {
            pstmt.executeUpdate();
        }
        System.out.println("Deleted health clients");
    }

    private void displayOptions() {
        for(Option option : Option.values()) {
            System.out.println(option.value + ": " + option.name().toLowerCase());
        }
        System.out.println();
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
        QUIT(20),
        DISPLAY_OPTIONS(21);

        final int value;
        Option(final int value) {
            this.value = value;
        }
    }
}
