import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.*;

import java.util.Map;
import java.util.Scanner;

import javax.xml.transform.Result;

import java.util.LinkedHashMap;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class InnReservations{
    private final String JDBC_URL = "jdbc:h2:~/csc365_lab7";
    private final String JDBC_USER = "";
    private final String JDBC_PASSWORD = "";
    public static void main(String[] args){
        try{
            InnReservations ir = new InnReservations();
            ir.initDb();
            ir.getUserInput();
        }
        catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
        }
        
    }

    private void getUserInput() throws SQLException{
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while(exit == false){
            printOptions();
            String input = scanner.nextLine();
            if(input.equals("1") ){
                getRooms();
            }
            else if(input.equals("2")){
                makeReservation();
            }
            else if(input.equals("3")){
                reservationChange();
            }
            else if(input.equals("4")){
                cancelReservation();
            }
            else if(input.equals("5")){
                revenueSummary();
            }
            else if(input.equals("6")){
                printOptions();
            }
            else if(input.equals("7")){
                System.out.println("Exiting...");
                exit = true;
            }
            else{
                System.out.println("Incorrect Option. Please enter different again");
            }
        }
        scanner.close();
    }

    private void printOptions(){
        System.out.println("\nPlease enter the number for one of the following options:");
        System.out.println("[1] Rooms and Rates");
        System.out.println("[2] Reservations");
        System.out.println("[3] Reservation Change");
        System.out.println("[4] Reservation Cancellation");
        System.out.println("[5] Revenue Summary");
        System.out.println("[6] Show options again");
        System.out.println("[7] Exit");
    }

    private void testing() throws SQLException{
        try (Connection conn = DriverManager.getConnection(JDBC_URL,
							   JDBC_USER,
							   JDBC_PASSWORD)) {
        String sql = "select *" +
        " from lab7_reservations l1"+
        " join lab7_reservations l2 on l2.Room = l1.Room and l2.CheckIn >= l1.Checkout";
        try (Statement stmt = conn.createStatement();
		 ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()){
                String rc = rs.getString("Room");
                //String na = rs.getString("l2.CheckIn");
                System.out.println(rc);
                //System.out.println(na);
            }
         }
        }
        
        
    }

    private void getRooms() throws SQLException{
        try (Connection conn = DriverManager.getConnection(JDBC_URL,
							   JDBC_USER,
							   JDBC_PASSWORD)) {

        String sql = "select *"+
        " from lab7_rooms r1"+
       " left outer join"+
       " (select Room as Today"+
       " from lab7_reservations"+
       " where Room not in"+
       " (select Room"+
       " from lab7_reservations"+
       " where CheckIn <= CURDATE() and Checkout > CURDATE())"+
       " group by Room) as T on T.Today = r1.RoomCode"+
       " left outer join"+
       " (select Room, min(CheckIn) as NextReservation"+
       " from"+
       " (select r2.Room, r2.CheckIn, r2.Checkout"+
       " from lab7_reservations r1"+
       " inner join lab7_reservations r2 on r1.Room = r2.Room and r1.CheckIn <> r2.CheckIn and r1.Checkout <> r2.Checkout and r2.CheckIn >= r1.Checkout"+
       " group by r2.Room, r2.CheckIn, r2.Checkout"+
       " having min(r2.CheckIn) >= CURDATE()) as T"+
       " group by Room) as T2 on T2.Room = r1.RoomCode"+
       " left outer join"+
       " (select Room, min(PleaseWork) as NextCheckIn"+
        " from"+
        " (select Room,"+
        " case when Dif > 0 then Checkout"+
        " when Dif = 0 then NextOut"+
        " end as PleaseWork"+
        " from"+
        " (select *, DATEDIFF(DAY, Checkout, NextRes) as Dif"+
        " from"+
        " (select r1.Room, r1.CheckIn, r1.Checkout, min(r2.CheckIn) as NextRes, min(r2.Checkout) as NextOut"+
        " from lab7_reservations r1"+
        " inner join lab7_reservations r2 on r2.Room = r1.Room and r2.CheckIn <> r1.CheckIn and r2.CheckIn >= r1.Checkout"+
        " group by r1.Room, r1.CheckIn, r1.Checkout) as T) as T) as T"+
        " group by Room) as T3 on T3.Room = r1.RoomCode"+
        " order by RoomCode";
       
        
	    try (Statement stmt = conn.createStatement();
		 ResultSet rs = stmt.executeQuery(sql);) {

            System.out.format("\n%-10s\t%-25s\t\t%-10s\t%-10s\t%-10s\t%-10s\t%-10s\t", "RoomCode", "RoomName", "Beds", "BedType", "MaxOcc", "BasePrice", "Decor");
            System.out.format("%-10s\t%-10s\n\n", "Availability", "NextReservation");
            //System.out.format("%-10s\t%-10s\n", "available", "next");
		while (rs.next()) {
		    String RoomCode = rs.getString("RoomCode");
            String RoomName = rs.getString("RoomName");
            int Beds = rs.getInt("Beds");
            String bedType = rs.getString("bedType");
            int maxOcc = rs.getInt("maxOcc");
            float basePrice = rs.getFloat("basePrice");
            String decor = rs.getString("decor");
            String available = rs.getString("Today");
            String next = rs.getString("NextReservation");
            String nextCheckIn = rs.getString("NextCheckIn");
            System.out.format("%-10s\t%-25s\t\t %-10d\t%-10s\t%-10d\t%-10s\t%-10s\t", RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor);


            if(available!=null){available="Today";} 
            else{
                available = nextCheckIn;
            }
            if(available==null){available="Today";}
            if(next==null){next="None";}
            System.out.format("%-10s\t%-10s\n", available, next);
        }
	    }
	}
    }

    private void makeReservation() throws SQLException{
        try (Connection conn = DriverManager.getConnection(JDBC_URL,
							   JDBC_USER,
							   JDBC_PASSWORD)) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("\nEnter your first name:\n");
            String firstname = scanner.nextLine();
            System.out.print("\nEnter your last name:\n");
            String lastname =scanner.nextLine();
            System.out.print("\nEnter desired room code :\n");
            String roomcode = scanner.nextLine();
            System.out.print("\nEnter CheckIn date (YYYY-MM-DD):\n");
            String checkin = scanner.nextLine();
            System.out.print("\nEnter Checkout date (YYYY-MM-DD):\n");
            String checkout = scanner.nextLine();
            System.out.print("\nEnter Number of Children:\n");
            int children = scanner.nextInt();
            System.out.print("\nEnter Number of Adults:\n");
            int adults = scanner.nextInt();
            boolean validReservation = false;
            float rate = 0;
            String roomname = "";
            String bedtype = "";
            String listOfRooms = "select * from lab7_reservations inner join lab7_rooms on lab7_rooms.RoomCode = lab7_reservations.Room where Room = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(listOfRooms)) {
                pstmt.setString(1, roomcode);
                
                ResultSet rs = pstmt.executeQuery();
                
                boolean available = true;
                while(rs.next()){
                    String rowCheckIn = rs.getString("CheckIn"); 
                    String rowCheckout = rs.getString("Checkout");
                    rate = rs.getFloat("Rate");
                    roomname = rs.getString("RoomName");
                    bedtype = rs.getString("bedType");
                    int maxOcc = rs.getInt("maxOcc");
                    if(checkin.compareTo(rowCheckIn) <= 0 && checkout.compareTo(rowCheckout) >= 0){
                        available = false;
                        break;
                    }
                    else if(checkin.compareTo(rowCheckIn) <= 0 && checkout.compareTo(rowCheckout) < 0 && checkout.compareTo(rowCheckIn) > 0){
                        available = false;
                        break;
                    }
                    else if(checkin.compareTo(rowCheckIn) >= 0 && checkout.compareTo(rowCheckout) >= 0 && checkin.compareTo(rowCheckout) < 0){
                        available = false;
                        break;
                    }
                    else if(checkin.compareTo(rowCheckIn) > 0 && checkin.compareTo(rowCheckout) < 0 && checkout.compareTo(rowCheckout) < 0 && checkout.compareTo(rowCheckIn) > 0){
                        available = false;
                        break;
                    }
                    else if(children + adults > maxOcc){
                        available = false;
                        break;
                    }
                }
                String maxCode = "select max(CODE) as CODE from lab7_reservations";
                int code = 0;
                try (Statement stmt = conn.createStatement();
		        ResultSet rs2 = stmt.executeQuery(maxCode)) {
                    while(rs2.next()){
                        code = rs2.getInt("CODE");
                        code += 1;
                    }
                }
                if(available){
                    conn.setAutoCommit(false);
                    String insertData = "INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement pstmt2 = conn.prepareStatement(insertData)) {
                        pstmt2.setInt(1, code);
                        pstmt2.setString(2, roomcode);
                        pstmt2.setDate(3, Date.valueOf(checkin));
                        pstmt2.setDate(4, Date.valueOf(checkout));
                        pstmt2.setFloat(5, rate);
                        pstmt2.setString(6, lastname);
                        pstmt2.setString(7, firstname);
                        pstmt2.setInt(8, adults);
                        pstmt2.setInt(9, children);
                        pstmt2.executeUpdate();

                        System.out.println("\nYour reservation has been confirmed");
                        System.out.println("Here is the information that you entered: ");
                        System.out.format("First, Last Name: %s, %s\n", firstname, lastname);
                        System.out.format("Room code, Room name, Bed Type: %s, %s, %s\n", roomcode, roomname, bedtype);
                        System.out.format("Begin and End Date of Stay: %s to %s\n", checkin, checkout);
                        System.out.format("Number of adults: %d\n", adults);
                        System.out.format("Number of children: %d\n", children);
                        double cost = 0;
                        LocalDate checkInDate = LocalDate.parse(checkin);
                        LocalDate checkoutDate = LocalDate.parse(checkout);
                        for(LocalDate date = checkInDate; date.isBefore(checkoutDate); date = date.plusDays(1)){
                            DayOfWeek day = date.getDayOfWeek();
                            if(day.getValue() >= 6){
                                cost += (rate * 1.10);
                            }
                            else{
                                cost += rate;
                            }
                        }
                        System.out.format("Total cost of stay: %,.2f\n", cost);
                        System.out.format("Reservation code: %d\n", code);
                        conn.commit();
                    }
                    catch (SQLException e) {
                        System.out.println("Error when trying to create reservation\n");
                        conn.rollback();
                    }
                    
                }
                else{
                    System.out.println("\nUnable to reserve room due to conflicting reservation dates or too many people in your reservation");
                }
                //System.out.println(available);
            }

            
        }
    }

    private void reservationChange() throws SQLException{
        try (Connection conn = DriverManager.getConnection(JDBC_URL,
							   JDBC_USER,
							   JDBC_PASSWORD)) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter your Reservation Code: ");
            int code = scanner.nextInt();

            String sql = "select * from lab7_reservations where CODE = ?";
            boolean isAReservation = true;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, code);
                ResultSet rs = pstmt.executeQuery();
                if(rs.next() == false){
                    isAReservation = false;
                }
            }

            boolean keepUpdating = true;
            if(isAReservation){
            Map<String, String> dictionary = new HashMap<String, String>();
            while(keepUpdating){
                System.out.println("Enter correspondng number to make a change to your reservation");
                System.out.println("1 - First Name");
                System.out.println("2 - Last Name");
                System.out.println("3 - Begin Date");
                System.out.println("4 - End Date");
                System.out.println("5 - Number of children");
                System.out.println("6 - Number of adults");
                System.out.println("7 - Finished");
                int option = scanner.nextInt();
                scanner.nextLine();
                switch(option){
                    case 1:
                    System.out.println("Enter new first name: ");
                    String firstname = scanner.nextLine();
                    dictionary.put("firstname", firstname);
                    break;
                    case 2:
                    System.out.println("Enter new last name: ");
                    String lastname = scanner.nextLine();
                    dictionary.put("lastname", lastname);
                    break;
                    case 3:
                    System.out.println("Enter new Checkin Date(YYYY-MM-DD): ");
                    String checkin = scanner.nextLine();
                    dictionary.put("checkin", checkin);
                    break;
                    case 4:
                    System.out.println("Enter new Checkout Date(YYYY-MM-DD): ");
                    String checkout = scanner.nextLine();
                    dictionary.put("checkout", checkout);
                    break;
                    case 5:
                    System.out.println("Enter new number of children: ");
                    String children = scanner.nextLine();
                    dictionary.put("children", children);
                    break;
                    case 6:
                    System.out.println("Enter new number of adults: ");
                    String adults = scanner.nextLine();
                    dictionary.put("adults", adults);
                    break;
                    case 7:
                    keepUpdating = false;
                    break;
                }
            }
            //System.out.println(Arrays.asList(dictionary));
            if(dictionary.isEmpty() == false){
                if(dictionary.containsKey("firstname")){
                    conn.setAutoCommit(false);
                    try(PreparedStatement ps = conn.prepareStatement("update lab7_reservations set FirstName = ? where CODE = ?")){
                        ps.setString(1, dictionary.get("firstname"));
                        ps.setInt(2, code);
                        ps.executeUpdate();
                        System.out.println("Successfully updated first name on your reservation");
                        conn.commit();
                    }catch (SQLException e) {
                        System.out.println("\nError updating first name\n");
                        conn.rollback();
                    }
                }
                if(dictionary.containsKey("lastname")){
                    conn.setAutoCommit(false);
                    try(PreparedStatement ps = conn.prepareStatement("update lab7_reservations set LastName = ? where CODE = ?")){
                        ps.setString(1, dictionary.get("lastname"));
                        ps.setInt(2, code);
                        ps.executeUpdate();
                        System.out.println("Successfully updated last name on your reservation");
                        conn.commit();
                    }catch (SQLException e) {
                        System.out.println("\nError updating last name\n");
                        conn.rollback();
                    }
                }
                if(dictionary.containsKey("checkin")){
                    conn.setAutoCommit(false);
                    try(PreparedStatement ps = conn.prepareStatement("update lab7_reservations set CheckIn = ? where CODE = ?")){
                        ps.setDate(1, Date.valueOf(dictionary.get("checkin")));
                        ps.setInt(2, code);
                        boolean valid = true;
                        try(PreparedStatement ps2 = conn.prepareStatement("select * from lab7_reservations where Room ="+
                        " (select Room"+
                        " from lab7_reservations"+
                        " where CODE = ?) and CODE <> ?")){
                            ps2.setInt(1, code);
                            ps2.setInt(2, code);
                            ResultSet rs = ps2.executeQuery();
                            while(rs.next()){
                                String cin = rs.getString("CheckIn");
                                String cout = rs.getString("Checkout");
                                //System.out.format("%s %s", cin, cout);
                                if(cin.compareTo(dictionary.get("checkin")) <= 0 && cout.compareTo(dictionary.get("checkin")) > 0){
                                    valid = false;
                                    System.out.println("\nInvalid Checkin Date provided due to time conflicts\n");
                                    break;
                                }
                            }
                        }
                        
                        if(valid==true){
                        System.out.println("\nSuccessfully updated checkin date\n");
                        ps.executeUpdate();
                        conn.commit();
                        }
                        
                    }catch (SQLException e) {
                        System.out.println("\nError updating checkin date\n");
                        conn.rollback();
                    }
                }

                if(dictionary.containsKey("checkout")){
                    conn.setAutoCommit(false);
                    try(PreparedStatement ps = conn.prepareStatement("update lab7_reservations set Checkout = ? where CODE = ?")){
                        ps.setString(1, dictionary.get("checkout"));
                        ps.setInt(2, code);
                        boolean valid = true;
                        try(PreparedStatement ps2 = conn.prepareStatement("select * from lab7_reservations where Room ="+
                        " (select Room"+
                        " from lab7_reservations"+
                        " where CODE = ?) and CODE <> ?")){
                            ps2.setInt(1, code);
                            ps2.setInt(2, code);
                            ResultSet rs = ps2.executeQuery();
                            while(rs.next()){
                                String cin = rs.getString("CheckIn");
                                String cout = rs.getString("Checkout");
                                //System.out.format("%s %s", cin, cout);
                                if(cin.compareTo(dictionary.get("checkout")) < 0 && cout.compareTo(dictionary.get("checkout")) > 0){
                                    valid = false;
                                    System.out.println("\nInvalid Checkout Date provided due to time conflicts\n");
                                    break;
                                }
                            }
                        }
                        if(valid==true)
                        {System.out.println("\nSuccessfully updated checkout date\n");
                        ps.executeUpdate();
                        conn.commit();}
                    }catch (SQLException e) {
                        System.out.println("\nError updating checkout date\n");
                        conn.rollback();
                    }
                }
                if(dictionary.containsKey("children")){
                    conn.setAutoCommit(false);
                    try(PreparedStatement ps = conn.prepareStatement("update lab7_reservations set Kids = ? where CODE = ?")){
                        ps.setInt(1, Integer.parseInt(dictionary.get("children")));
                        ps.setInt(2, code);
                        ps.executeUpdate();
                        System.out.println("\nSuccessfully updated number of children\n");
                        conn.commit();
                    }catch (SQLException e) {
                        System.out.println("\nError updating number of children\n");
                        conn.rollback();
                    }
                }
                if(dictionary.containsKey("adults")){
                    conn.setAutoCommit(false);
                    try(PreparedStatement ps = conn.prepareStatement("update lab7_reservations set Adults = ? where CODE = ?")){
                        ps.setInt(1, Integer.parseInt(dictionary.get("adults")));
                        ps.setInt(2, code);
                        ps.executeUpdate();
                        System.out.println("\nSuccessfully updated number of adults\n");
                        conn.commit();
                    }catch (SQLException e) {
                        System.out.println("\nError updating number of adults\n");
                        conn.rollback();
                    }
                }
            }
            }
            else{
                System.out.println("The code you entered is not a valid reservation code");
            }
            
        }
        
    }

    private void cancelReservation() throws SQLException{
        try (Connection conn = DriverManager.getConnection(JDBC_URL,
                                   JDBC_USER,
                                   JDBC_PASSWORD)) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("\nEnter the reservation code you would like to CANCEL: ");
            int code = scanner.nextInt();
            scanner.nextLine();
            System.out.format("Are you sure you want to cancel reservation (%s): [Y/N]\n", code);
            String confirm = scanner.nextLine();
            boolean validCode = true;
            try(PreparedStatement ps2 = conn.prepareStatement("select * from lab7_reservations where CODE = ?")){
                ps2.setInt(1, code);
                ResultSet rs = ps2.executeQuery();
                if(rs.next() == false){
                    validCode = false;
                }
            }
            if(confirm.toUpperCase().equals("Y") && validCode == true){
                try(PreparedStatement ps = conn.prepareStatement("delete from lab7_reservations where CODE = ?")){
                    ps.setInt(1, code);
                    ps.execute();
                    System.out.println("Successfully canceled your reservation");
                }
            }
            else{
                System.out.println("\nReservation has already been canceled or Invalid Reservation Code");
            }
        }
    }

    private void revenueSummary() throws SQLException{
        try (Connection conn = DriverManager.getConnection(JDBC_URL,
                                   JDBC_USER,
                                   JDBC_PASSWORD)) {
        String sql = "with revenue as (select Room, "+
        " ROUND(sum(case when month(Checkout) = 1 then datediff(day, checkin, checkout) * rate else 0 end), 0) as January,"+
        " ROUND(sum(case when month(Checkout) = 2 then datediff(day, checkin, checkout) * rate else 0 end), 0) as February,"+
        " ROUND(sum(case when month(Checkout) = 3 then datediff(day, checkin, checkout) * rate else 0 end), 0) as March,"+
        " ROUND(sum(case when month(Checkout) = 4 then datediff(day, checkin, checkout) * rate else 0 end), 0) as April,"+
        " ROUND(sum(case when month(Checkout) = 5 then datediff(day, checkin, checkout) * rate else 0 end), 0) as May,"+
        " ROUND(sum(case when month(Checkout) = 6 then datediff(day, checkin, checkout) * rate else 0 end), 0) as June,"+
        " ROUND(sum(case when month(Checkout) = 7 then datediff(day, checkin, checkout) * rate else 0 end), 0) as July,"+
        " ROUND(sum(case when month(Checkout) = 8 then datediff(day, checkin, checkout) * rate else 0 end), 0) as August,"+
        " ROUND(sum(case when month(Checkout) = 9 then datediff(day, checkin, checkout) * rate else 0 end), 0) as September,"+
        " ROUND(sum(case when month(Checkout) = 10 then datediff(day, checkin, checkout) * rate else 0 end), 0) as October,"+
        " ROUND(sum(case when month(Checkout) = 11 then datediff(day, checkin, checkout) * rate else 0 end), 0) as November,"+
        " ROUND(sum(case when month(Checkout) = 12 then datediff(day, checkin, checkout) * rate else 0 end), 0) as December,"+
        " ROUND(sum(datediff(DAY, CheckIn, Checkout) * rate), 0) as Annual"+
        " from lab7_reservations"+
        " where YEAR(Checkout) = YEAR(CURDATE())"+
        " group by Room)"+
        " select Room, January, February, March, April, May, June, July, August, September, October, November, December, Annual from revenue"+
        " union"+
        " select 'Total', sum(January), sum(February), sum(March), sum(April), sum(May), sum(June), sum(July), sum(August), sum(September), sum(October), sum(November), sum(December), sum(Annual)"+
        " from revenue";

        try (Statement stmt = conn.createStatement();
		 ResultSet rs = stmt.executeQuery(sql);) {
            System.out.println("Revenue in $ (Did not apply a weekend rate to calculations)");
            System.out.format("%-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s\n\n", "Room", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Annual");
            while(rs.next()){
                String room = rs.getString("Room");
                String jan = rs.getString("January");
                String feb = rs.getString("February");
                String mar = rs.getString("March");
                String apr = rs.getString("April");
                String may = rs.getString("May");
                String jun = rs.getString("June");
                String jul = rs.getString("July");
                String aug = rs.getString("August");
                String sep = rs.getString("September");
                String oct = rs.getString("October");
                String nov = rs.getString("November");
                String dec = rs.getString("December");
                String ann = rs.getString("Annual"); 
                System.out.format("%-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s\n", room, jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec, ann);
            
            }
         }

        }

    }

    private void initDb() throws SQLException {
        try (Connection conn = DriverManager.getConnection(JDBC_URL,
                                   JDBC_USER,
                                   JDBC_PASSWORD)) {
            try (Statement stmt = conn.createStatement()) {
                    stmt.execute("DROP TABLE IF EXISTS lab7_reservations");
                    stmt.execute("DROP TABLE IF EXISTS lab7_rooms");
                    stmt.execute("CREATE TABLE lab7_rooms (RoomCode CHAR(5) NOT NULL, RoomName VARCHAR(30), Beds INTEGER, bedType VARCHAR(8), maxOcc INTEGER, basePrice DECIMAL(7, 2), decor VARCHAR(20), PRIMARY KEY(RoomCode), UNIQUE(RoomName))");
                    stmt.execute("CREATE TABLE lab7_reservations (CODE INTEGER NOT NULL, Room CHAR(5), CheckIn DATE, Checkout DATE, Rate DECIMAL(7, 2), LastName VARCHAR(15), FirstName VARCHAR(15), Adults INTEGER, Kids INTEGER, PRIMARY KEY(CODE), FOREIGN KEY (Room) references lab7_rooms(RoomCode))");
                    stmt.execute("INSERT INTO lab7_rooms (RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor) VALUES ('AOB', 'Abscond or bolster',	2, 'Queen', 4, 175, 'traditional')");
                    stmt.execute("INSERT INTO lab7_rooms (RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor) VALUES ('CAS', 'Convoke and sanguine', 2, 'King', 4,	175, 'traditional')");
                    stmt.execute("INSERT INTO lab7_rooms (RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor) VALUES ('FNA', 'Frugal not apropos',	2, 'King',	4,	250, 'traditional')");
                    stmt.execute("INSERT INTO lab7_rooms (RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor) VALUES ('HBB',	'Harbinger but bequest',	1,	'Queen',	2,	100,	'modern')");
                    stmt.execute("INSERT INTO lab7_rooms (RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor) VALUES ('IBD',	'Immutable before decorum',	2,	'Queen',	4,	150,	'rustic')");
                    stmt.execute("INSERT INTO lab7_rooms (RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor) VALUES ('TAA',	'Thrift and accolade',	1,	'Double',	2,	75,	'modern')");
                    stmt.execute("INSERT INTO lab7_rooms (RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor) VALUES ('RND',	'Recluse and defiance',	1,	'King',	2,	150,	'modern')");
                    stmt.execute("INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (10105,	'HBB',	'2021-03-09',	'2021-03-25',	100,	'SELBIG',	'CONRAD',	1,	0)");
                    stmt.execute("INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (10106,	'HBB',	'2021-03-25',	'2021-10-25',	100,	'BOB',	'JOHN',	1,	0)");
                    stmt.execute("INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (10109,	'CAS',	'2021-10-23',	'2021-10-25',	100,	'BOBBY',	'BURNS',	1,	0)");
                    stmt.execute("INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (10183,	'IBD',	'2021-09-19',	'2021-09-20',	150,	'GABLER',	'DOLLIE',	2,	0)");
                    stmt.execute("INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (10489,	'AOB',	'2021-01-02',	'2021-01-09',	218.75,	'CARISTO',	'MARKITA',	2,	1)");
                    stmt.execute("INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (10449,	'RND',	'2021-03-15',	'2021-03-20',	150,	'KLESS',	'NELSON',	1,	0)");
                    stmt.execute("INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (10450,	'RND',	'2021-03-20',	'2021-03-25',	150,	'SMITH',	'KELLY',	1,	0)");
                    stmt.execute("INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (10451,	'RND',	'2021-03-29',	'2021-03-30',	150,	'SMITH',	'DRAKE',	1,	0)");
                    stmt.execute("INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (10574,	'FNA',	'2021-03-06',	'2021-03-09',	287.5,	'SWEAZY',	'ROY',	2,	1)");
                    stmt.execute("INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (10575,	'FNA',	'2021-04-06',	'2021-04-09',	287.5,	'ABNER',	'SETH',	2,	1)");
                    stmt.execute("INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (10576,	'FNA',	'2021-04-20',	'2021-04-21',	287.5,	'KANE',	'ROBERT',	2,	1)");
            }
        }
    }
}