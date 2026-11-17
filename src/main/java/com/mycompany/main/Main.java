package com.mycompany.main;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * QuickChat console app.
 * Part 1: register and log in. Part 2: send, store or disregard messages.
 * Part 3: Stored Messages menu (search, delete and report on the stored messages).
 */
public class Main {

    static Scanner input = new Scanner(System.in);

    public static void main(String[] args) {

        // Part 1: register, then log in
        Login login = registerAccount();

        if (!logIn(login)) {
            System.out.println("Too many failed attempts. Goodbye.");
            return;
        }

        // Part 2: only reached after a successful login
        System.out.println("\nWelcome to QuickChat.");
        int numMessages = askNumberOfMessages();

        // Part 3: read back any messages stored in the JSON file on a previous run
        int loaded = Message.loadStoredMessages();
        if (loaded > 0) {
            System.out.println(loaded + " stored message(s) loaded from the file.");
        }

        runMenu(numMessages, login);
    }

    // Asks for name and details, repeating each question until it is valid
    static Login registerAccount() {
        System.out.print("Enter your first name: ");
        String firstName = input.nextLine().trim();
        System.out.print("Enter your last name: ");
        String lastName = input.nextLine().trim();

        Login login = new Login(firstName, lastName);

        String username;
        do {
            System.out.print("Enter your username: ");
            username = input.nextLine();
            System.out.println(login.usernameStatus(username));
        } while (!login.checkUserName(username));

        String password;
        do {
            System.out.print("Enter your password: ");
            password = input.nextLine();
            System.out.println(login.passwordStatus(password));
        } while (!login.checkPasswordComplexity(password));

        String cellPhone;
        do {
            System.out.print("Enter your cell phone number (e.g. +27831234567): ");
            cellPhone = input.nextLine();
            System.out.println(login.cellPhoneStatus(cellPhone));
        } while (!Login.checkCellPhoneNumber(cellPhone));

        System.out.println(login.registerUser(username, password, cellPhone));
        return login;
    }

    // Gives the user three tries to log in
    static boolean logIn(Login login) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            System.out.print("\nEnter your username to login: ");
            String username = input.nextLine();
            System.out.print("Enter your password to login: ");
            String password = input.nextLine();

            boolean success = login.loginUser(username, password);
            System.out.println(login.returnLoginStatus(success));

            if (success) {
                return true;
            }
        }
        return false;
    }

    // Asks how many messages the user wants to enter (must be a whole number above 0)
    static int askNumberOfMessages() {
        while (true) {
            System.out.print("How many messages do you want to enter? ");
            try {
                int number = Integer.parseInt(input.nextLine().trim());
                if (number > 0) {
                    return number;
                }
            } catch (NumberFormatException e) {
                // not a number, fall through and ask again
            }
            System.out.println("Please enter a whole number greater than 0.");
        }
    }

    // Main menu, keeps running until the user picks Quit
    static void runMenu(int numMessages, Login login) {
        int messagesEntered = 0;
        boolean running = true;

        while (running) {
            System.out.println("\n1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");
            System.out.print("Choose an option: ");
            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    if (messagesEntered >= numMessages) {
                        System.out.println("You have already entered all " + numMessages + " messages.");
                    } else {
                        messagesEntered = sendMessages(numMessages, messagesEntered);
                    }
                    break;
                case "2":
                    System.out.println("Coming Soon.");
                    break;
                case "3":
                    System.out.println("Total messages sent: " + Message.returnTotalMessages());
                    System.out.println("Goodbye.");
                    running = false;
                    break;
                case "4":
                    storedMessagesMenu(login);
                    break;
                default:
                    System.out.println("Invalid option, please choose 1, 2, 3 or 4.");
            }
        }
    }

    // Loops until the set number of messages has been entered. Returns the new count.
    static int sendMessages(int numMessages, int startAt) {
        for (int i = startAt; i < numMessages; i++) {
            System.out.println("\n--- Message " + (i + 1) + " of " + numMessages + " ---");

            String recipient = readRecipient();
            String text = readMessageText();

            Message message = new Message(i, recipient, text);
            System.out.println(message.getMessageIDMessage());

            handleChoice(message);
        }

        System.out.println("\nTotal messages sent: " + Message.returnTotalMessages());
        return numMessages;
    }

    // Keeps asking until the recipient number is valid
    static String readRecipient() {
        while (true) {
            System.out.print("Enter the recipient's cell number: ");
            String recipient = input.nextLine().trim();
            String result = Message.recipientStatus(recipient);
            System.out.println(result);
            if (result.equals(Message.RECIPIENT_OK)) {
                return recipient;
            }
        }
    }

    // Keeps asking until the message is 250 characters or less
    static String readMessageText() {
        while (true) {
            System.out.print("Enter your message: ");
            String text = input.nextLine();
            if (text.trim().isEmpty()) {
                System.out.println("The message cannot be empty.");
                continue;
            }
            if (text.length() > Message.MAX_LENGTH) {
                System.out.println("Please enter a message of less than 250 characters.");
                System.out.println(Message.checkMessageLength(text));
            } else {
                System.out.println(Message.checkMessageLength(text));
                return text;
            }
        }
    }

    // Send, disregard or store the message
    static void handleChoice(Message message) {
        int choice = 0;
        while (choice < Message.SEND || choice > Message.STORE) {
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            System.out.print("Choose an option: ");
            try {
                choice = Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException e) {
                choice = 0;
            }
        }

        System.out.println(message.sentMessage(choice));

        if (choice == Message.SEND) {
            System.out.println("\n" + message.getDetails());
        } else if (choice == Message.DISREGARD) {
            String answer = input.nextLine().trim();
            while (!answer.equals("0")) {
                System.out.println("Press 0 to delete the message.");
                answer = input.nextLine().trim();
            }
            message.disregardMessage();
            System.out.println("Message deleted.");
        }
    }

    // Part 3: Stored Messages menu, keeps running until the user goes back
    static void storedMessagesMenu(Login login) {
        boolean back = false;

        while (!back) {
            System.out.println("\n--- Stored Messages ---");
            System.out.println("1) Display the sender and recipient of all stored messages");
            System.out.println("2) Display the longest stored message");
            System.out.println("3) Search for a message ID");
            System.out.println("4) Search for all messages to a recipient");
            System.out.println("5) Delete a message using its hash");
            System.out.println("6) Display a report of the messages");
            System.out.println("7) Back to the main menu");
            System.out.print("Choose an option: ");
            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.print(Message.displayStoredSendersAndRecipients(login.getRegisteredUsername()));
                    break;
                case "2":
                    System.out.println("Longest stored message: " + Message.getLongestStoredMessage());
                    break;
                case "3":
                    System.out.print("Enter the message ID: ");
                    Message found = Message.searchByMessageID(input.nextLine().trim());
                    if (found == null) {
                        System.out.println("No message was found with that ID.");
                    } else {
                        System.out.println("Recipient: " + found.getRecipient());
                        System.out.println("Message: " + found.getText());
                    }
                    break;
                case "4":
                    System.out.print("Enter the recipient's cell number: ");
                    ArrayList<String> texts = Message.searchByRecipient(input.nextLine().trim());
                    if (texts.isEmpty()) {
                        System.out.println("No messages were found for that recipient.");
                    } else {
                        for (String text : texts) {
                            System.out.println("Message: " + text);
                        }
                    }
                    break;
                case "5":
                    System.out.print("Enter the message hash: ");
                    System.out.println(Message.deleteByHash(input.nextLine().trim()));
                    break;
                case "6":
                    System.out.println();
                    System.out.print(Message.getSentReport());
                    System.out.println();
                    System.out.print(Message.getStoredReport());
                    break;
                case "7":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option, please choose a number from 1 to 7.");
            }
        }
    }
}
