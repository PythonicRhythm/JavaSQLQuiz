package quiz;

import java.sql.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hello world!
 */
public final class Quiz {

    private final String DB_URL = "jdbc:mysql://localhost:3306/quiz";
    private final String Username = "root";
    private final String Password = "root";
    private Statement sqlSt;
    private Connection dbConnect;
    private int difficulty;
    private String playerName;
    private int totalScore;


    public Quiz() {
        attemptConnection();
        initializeDifficulty();
        gatherPlayerName();
        promptQuestions();
        displayLeaderboard();
    }

    private void displayLeaderboard() {
        String insertNewScore = "insert into leaderboard (Name, Score) values ('"+playerName+"', "+totalScore+");";
        String gatherEntries = "Select * from leaderboard;";
        String output;
        ResultSet allBoardEntries;
        try {
            sqlSt.executeUpdate(insertNewScore);
            allBoardEntries = sqlSt.executeQuery(gatherEntries);

            System.out.println("\nLeaderboard:");
            while(allBoardEntries.next()) {
                output = allBoardEntries.getString("Name") + ": " + allBoardEntries.getInt("Score");
                System.out.println(output);
            }

        }
        catch(SQLException ex) {
            System.out.println("Leaderboard insertion failed: " + ex.getMessage());
        }
    }

    private void initializeLeaderboard() {
        String createTableSQL = "Create table if not exists leaderboard (" +
                                    "   QuestionID int PRIMARY KEY AUTO_INCREMENT," +
                                    "   Name varchar(456) NOT NULL," +
                                    "   Score int NOT NULL" +
                                    ");";
        try {
            sqlSt.executeUpdate(createTableSQL);
        }
        catch(SQLException ex) {
            System.out.println("Create table failed: " + ex.getMessage());
        }
    }

    private void gatherPlayerName() {
        Scanner consoleReader = new Scanner(System.in);
        System.out.println("\nWhat is your name?");
        while(true) {
            System.out.print("> ");

            String response = consoleReader.nextLine().strip();
            if(response.length() > 50) {
                System.out.println("Name is too long, has to be less than 50 characters.");
            }
            else {
                playerName = response;
                return;
            }
        }
    }

    private void attemptConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            dbConnect = DriverManager.getConnection(DB_URL, Username, Password);
            sqlSt = dbConnect.createStatement();    // Allows SQL to be executed

            initializeLeaderboard();
            System.out.println("Connection with database successful!");
            System.out.println("\nWelcome to my SQL Quiz!");

        }
        catch(ClassNotFoundException ex) {
            Logger.getLogger(Quiz.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Class not found, check the JAR");
        }
        catch(SQLException ex) {
            Logger.getLogger(Quiz.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("SQL is bad! " + ex.getMessage());
        }
    }

    private void initializeDifficulty() {
        Scanner consoleReader = new Scanner(System.in);
        System.out.println("What difficulty do you choose?\n1 - Easy\n2 - Medium\n3 - Hard");
        while(true) {
            System.out.print("> ");


            int setting;
            try {
                setting = Integer.parseInt(consoleReader.nextLine());
                if(setting == 1) {
                    difficulty = 1;
                    return;
                }
                else if(setting == 2) {
                    difficulty = 2;
                    return;
                }
                else if(setting == 3) {
                    difficulty = 3;
                    return;
                }
                else {
                    System.out.println("Please enter 1, 2, or 3.");
                }
            }
            catch(NumberFormatException ex) {
                System.out.println("Please enter 1, 2, or 3.");
            }
        }
    }

    private void promptQuestions() {
        Scanner consoleReader = new Scanner(System.in);
        String output;
        String SQL = "select * from questions\nwhere Difficulty = "+difficulty+";";
        ResultSet result;   // Holds output from SQL
        try {
  
            result = sqlSt.executeQuery(SQL);
            // Result holds the output from the SQL
            int questionCounter = 1;
            while(result.next() != false) {
                output = result.getString("Question");
                System.out.println("\nQuestion "+questionCounter+":\n"+output);
                System.out.print("> ");

                String response = consoleReader.nextLine().toLowerCase().strip();
                if(response.equals(result.getString("Answer"))) {
                    totalScore += difficulty*1;
                    System.out.println("Correct! You've scored "+difficulty*1+" points!");
                }
                else {
                    System.out.println("Incorrect! The correct answer was: " + result.getString("Answer"));
                }

                questionCounter++;
            }

            consoleReader.close();

        }
        catch(SQLException ex) {
            System.out.println("Error reading from database: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        Quiz firstQuiz = new Quiz();
    }
}
