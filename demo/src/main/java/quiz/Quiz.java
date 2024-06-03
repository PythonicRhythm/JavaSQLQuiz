package quiz;

import java.sql.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Quiz class represents a mySQL quiz game that I created. The class reads
 * questions from a local database and prompts the user with these questions
 * looking for the correct answer. If the user types the correct answer, their score
 * raises and if they didnt, they get shown the correct answer. There are difficulty
 * settings for harder questions with better score boosts. Finally, a leaderboard
 * is shown that is retrieved from the SQL database and the user's score is
 * sent into the database.
 */
public final class Quiz {

    private final String DB_URL = "jdbc:mysql://localhost:3306/quiz";       // The URL of the database the Quiz is working from.s
    private final String Username = "root";                                 // Username 
    private final String Password = "root";                                 // Password
    private Statement sqlSt;                                                // Statement used to execute sql commands using the database.
    private Connection dbConnect;                                           // The bridge between the database and this java file.
    private int difficulty;                                                 // Difficulty setting set by player.
    private String playerName;                                              // Player's name used to insert them into the leaderboard.
    private int totalScore;                                                 // Total score the player accrued, sent into the leaderboard.


    // Constructor loop.
    public Quiz() {
        attemptConnection();
        initializeDifficulty();
        gatherPlayerName();
        promptQuestions();
        displayLeaderboard();
    }

    // displayLeaderboard() will insert the player's score into the db
    // and show the current leaderboard's player and scores via the terminal.
    private void displayLeaderboard() {
        String insertNewScore = "insert into leaderboard (Name, Score) values ('"+playerName+"', "+totalScore+");";
        String gatherEntries = "Select * from leaderboard order by Score desc;";
        ResultSet allBoardEntries;
        try {
            sqlSt.executeUpdate(insertNewScore);
            allBoardEntries = sqlSt.executeQuery(gatherEntries);

            System.out.println("\nLEADERBOARD:");
            while(allBoardEntries.next()) {
                String name = allBoardEntries.getString("Name");
                int score = allBoardEntries.getInt("Score");
                System.out.format("%35s%10d", name, score);
            }

        }
        catch(SQLException ex) {
            System.out.println("Leaderboard insertion failed: " + ex.getMessage());
        }
    }

    // initializeLeaderboard() will create the leaderboard table in the db
    // if not previously created.
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

    // gatherPlayerName() will gather the player's name via terminal
    // for later use in saving their score in the db leaderboard table.
    private void gatherPlayerName() {
        Scanner consoleReader = new Scanner(System.in);
        System.out.println("\nWhat is your name?");
        while(true) {
            System.out.print("> ");

            String response = consoleReader.nextLine().strip();
            if(response.length() > 30) {
                System.out.println("Name is too long, can't be greater than 30 characters.");
            }
            else {
                playerName = response;
                return;
            }
        }
    }

    // attemptConnection() will attempt to connect to the db given
    // the DB_URL, Username, and Password set in the member variables.
    // If class not found, the user will know. If there is an error
    // with the sql code the user will know.
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
            System.exit(0);
        }
        catch(SQLException ex) {
            Logger.getLogger(Quiz.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("SQL is bad! " + ex.getMessage());
            System.exit(0);
        }
    }

    // initializeDifficulty() will gather the difficulty setting of 
    // the quiz from the user via terminal. This setting will determine
    // the questions the user will receive and the score they will 
    // receive from each correct question.
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


    // promptQuestions() will display every single question from the user
    // provided difficulty setting and ask the user for the correct answer.
    // If the user provides the correct answer, they will receive a score
    // boost. If not, the correct answer will be displayed to the user.
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
