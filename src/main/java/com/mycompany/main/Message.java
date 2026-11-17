package com.mycompany.main;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Parts 2 and 3: one chat message plus the lists that track what has been sent,
 * disregarded or stored.
 */
public class Message {

    public static final int MAX_LENGTH = 250;

    public static final String RECIPIENT_OK = "Cell phone number successfully captured.";
    public static final String RECIPIENT_BAD = "Cell phone number is incorrectly formatted or does not contain "
            + "an international code. Please correct the number and try again.";
    public static final String LENGTH_OK = "Message ready to send.";

    public static final int SEND = 1;
    public static final int DISREGARD = 2;
    public static final int STORE = 3;

    // Shared by every message while the program runs
    private static final ArrayList<Message> sentMessages = new ArrayList<>();
    private static final ArrayList<Message> storedMessages = new ArrayList<>();

    // Part 3: disregarded messages, plus every hash and ID that has been created
    private static final ArrayList<Message> disregardedMessages = new ArrayList<>();
    private static final ArrayList<String> messageHashes = new ArrayList<>();
    private static final ArrayList<String> messageIDs = new ArrayList<>();
    private static int totalMessagesSent = 0;
    private static String storageFile = "messages.json";

    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String text;
    private final String messageHash;

    // Used by the app: the ID is generated for us
    public Message(int messageNumber, String recipient, String text) {
        this(generateMessageID(), messageNumber, recipient, text);
    }

    // Used by the tests, where we need to know the ID up front
    public Message(String messageID, int messageNumber, String recipient, String text) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.text = text;
        this.messageHash = createMessageHash();
    }

    // Random ten-digit number. Padded with zeros so it is always 10 characters.
    public static String generateMessageID() {
        long number = ThreadLocalRandom.current().nextLong(10_000_000_000L);
        return String.format("%010d", number);
    }

    // ID must not be longer than ten characters
    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }

    // Re-uses the cell number check from Login
    public String checkRecipientCell() {
        return recipientStatus(recipient);
    }

    // Same check on any number, so Main can use it before a Message exists
    public static String recipientStatus(String number) {
        return Login.checkCellPhoneNumber(number) ? RECIPIENT_OK : RECIPIENT_BAD;
    }

    // Tells the user if the text is short enough, or by how much it is too long
    public static String checkMessageLength(String text) {
        if (text.length() <= MAX_LENGTH) {
            return LENGTH_OK;
        }
        int extra = text.length() - MAX_LENGTH;
        return "Message exceeds " + MAX_LENGTH + " characters by " + extra + "; please reduce the size.";
    }

    public String getMessageIDMessage() {
        return "Message ID generated: " + messageID;
    }

    // First two digits of the ID : message number : first word + last word, all caps
    // e.g. 00:0:HITONIGHT
    public String createMessageHash() {
        String[] words = text.trim().split("\\s+");
        String firstWord = stripPunctuation(words[0]);
        String lastWord = stripPunctuation(words[words.length - 1]);
        return messageID.substring(0, 2) + ":" + messageNumber + ":"
                + (firstWord + lastWord).toUpperCase();
    }

    // Removes things like the comma in "Mike," or the ? in "tonight?"
    private static String stripPunctuation(String word) {
        return word.replaceAll("[^a-zA-Z0-9]", "");
    }

    // Does what the user picked: 1 = send, 2 = disregard, 3 = store
    public String sentMessage(int choice) {
        switch (choice) {
            case SEND:
                addMessage(this, SEND);
                return "Message successfully sent.";
            case DISREGARD:
                return "Press 0 to delete the message.";
            case STORE:
                if (storeMessage()) {
                    return "Message successfully stored.";
                }
                return "Message could not be stored.";
            default:
                return "Invalid option.";
        }
    }

    // All sent messages in one string
    public static String printMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent yet.";
        }
        StringBuilder all = new StringBuilder();
        for (Message m : sentMessages) {
            all.append(m.getDetails()).append("\n");
        }
        return all.toString();
    }

    public static int returnTotalMessages() {
        return totalMessagesSent;
    }

    // Details in the order the PoE asks for
    public String getDetails() {
        return "Message ID: " + messageID + "\n"
                + "Message Hash: " + messageHash + "\n"
                + "Recipient: " + recipient + "\n"
                + "Message: " + text + "\n";
    }

    // Adds this message to the stored list and writes the whole list to the JSON file.
    // TODO (yours to do): research JSON writing and reference your source here.
    // The rubric takes marks off if the JSON code is not attributed.
    public boolean storeMessage() {
        addMessage(this, STORE);
        if (saveStoredMessages()) {
            return true;
        }
        // The file could not be written, so undo the change to the lists
        storedMessages.remove(this);
        messageHashes.remove(messageHash);
        messageIDs.remove(messageID);
        return false;
    }

    // Writes every stored message to the JSON file. Also used when a stored message is deleted.
    private static boolean saveStoredMessages() {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < storedMessages.size(); i++) {
            json.append(storedMessages.get(i).toJson());
            if (i < storedMessages.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("]\n");

        try {
            Files.writeString(Path.of(storageFile), json.toString(), StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private String toJson() {
        return "  {\n"
                + "    \"messageID\": \"" + escape(messageID) + "\",\n"
                + "    \"messageNumber\": " + messageNumber + ",\n"
                + "    \"messageHash\": \"" + escape(messageHash) + "\",\n"
                + "    \"recipient\": \"" + escape(recipient) + "\",\n"
                + "    \"message\": \"" + escape(text) + "\"\n"
                + "  }";
    }

    // Escapes quotes, slashes and line breaks so the JSON stays valid
    private static String escape(String value) {
        StringBuilder out = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"':  out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        return out.toString();
    }

    // ---------------- Part 3 ----------------

    // Adds a message to the list that matches its flag (SEND, DISREGARD or STORE) and records
    // its hash and ID. The unit tests use this to load the PoE test data without typing it in.
    public static void addMessage(Message message, int flag) {
        if (flag == SEND) {
            sentMessages.add(message);
            totalMessagesSent++;
        } else if (flag == DISREGARD) {
            disregardedMessages.add(message);
        } else if (flag == STORE) {
            storedMessages.add(message);
        } else {
            return;
        }
        messageHashes.add(message.messageHash);
        messageIDs.add(message.messageID);
    }

    // Called after the user presses 0 to delete a message, keeps it in the disregarded list
    public void disregardMessage() {
        addMessage(this, DISREGARD);
    }

    // Finds a sent or stored message using its message ID, returns null if there is no match
    public static Message searchByMessageID(String id) {
        for (Message message : sentMessages) {
            if (message.messageID.equals(id)) {
                return message;
            }
        }
        for (Message message : storedMessages) {
            if (message.messageID.equals(id)) {
                return message;
            }
        }
        return null;
    }

    // Collects the text of every sent or stored message for one recipient
    public static ArrayList<String> searchByRecipient(String recipient) {
        ArrayList<String> found = new ArrayList<>();
        for (Message message : sentMessages) {
            if (message.recipient.equals(recipient)) {
                found.add(message.text);
            }
        }
        for (Message message : storedMessages) {
            if (message.recipient.equals(recipient)) {
                found.add(message.text);
            }
        }
        return found;
    }

    // Returns the text of the longest stored message
    public static String getLongestStoredMessage() {
        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }
        Message longest = storedMessages.get(0);
        for (Message message : storedMessages) {
            if (message.text.length() > longest.text.length()) {
                longest = message;
            }
        }
        return longest.text;
    }

    // Lists the sender and recipient of every stored message
    public static String displayStoredSendersAndRecipients(String sender) {
        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }
        StringBuilder output = new StringBuilder();
        for (Message message : storedMessages) {
            output.append("Sender: ").append(sender)
                    .append(" | Recipient: ").append(message.recipient).append("\n");
        }
        return output.toString();
    }

    // Deletes a stored or sent message using its hash and tidies up the hash and ID lists
    public static String deleteByHash(String hash) {
        for (int i = 0; i < storedMessages.size(); i++) {
            if (storedMessages.get(i).messageHash.equals(hash)) {
                Message removed = storedMessages.remove(i);
                messageHashes.remove(removed.messageHash);
                messageIDs.remove(removed.messageID);
                // Keep the JSON file in step with the stored list
                saveStoredMessages();
                return "Message: \"" + removed.text + "\" successfully deleted.";
            }
        }
        for (int i = 0; i < sentMessages.size(); i++) {
            if (sentMessages.get(i).messageHash.equals(hash)) {
                Message removed = sentMessages.remove(i);
                messageHashes.remove(removed.messageHash);
                messageIDs.remove(removed.messageID);
                return "Message: \"" + removed.text + "\" successfully deleted.";
            }
        }
        return "No message was found with that hash.";
    }

    // Report of all sent messages
    public static String getSentReport() {
        return buildReport("Sent messages report", sentMessages);
    }

    // Report with the full details of all stored messages
    public static String getStoredReport() {
        return buildReport("Stored messages report", storedMessages);
    }

    // Builds the text for a report from one of the lists
    private static String buildReport(String title, ArrayList<Message> list) {
        StringBuilder report = new StringBuilder("--- " + title + " ---\n");
        if (list.isEmpty()) {
            return report.append("No messages to show.\n").toString();
        }
        for (int i = 0; i < list.size(); i++) {
            Message message = list.get(i);
            report.append(i + 1).append(") Message ID: ").append(message.messageID).append("\n")
                    .append("   Message Hash: ").append(message.messageHash).append("\n")
                    .append("   Recipient: ").append(message.recipient).append("\n")
                    .append("   Message: ").append(message.text).append("\n");
        }
        return report.toString();
    }

    // Reads the JSON file back into the stored list, returns how many messages were loaded.
    // TODO (yours to do): research reading JSON and reference your source here.
    // The rubric takes marks off if the JSON code is not attributed.
    public static int loadStoredMessages() {
        Path path = Path.of(storageFile);
        if (!Files.exists(path)) {
            return 0;
        }
        String json;
        try {
            json = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return 0;
        }

        int loaded = 0;
        int[] pos = {0};
        while (true) {
            int open = json.indexOf('{', pos[0]);
            if (open == -1) {
                break;
            }
            pos[0] = open + 1;

            String id = "";
            int number = 0;
            String recipient = "";
            String text = "";

            // Read "key": value pairs until the closing brace of this message
            while (pos[0] < json.length() && json.charAt(pos[0]) != '}') {
                if (json.charAt(pos[0]) != '"') {
                    pos[0]++;
                    continue;
                }
                String key = readJsonString(json, pos);
                int colon = json.indexOf(':', pos[0]);
                if (colon == -1) {
                    break;
                }
                pos[0] = colon + 1;
                while (pos[0] < json.length() && Character.isWhitespace(json.charAt(pos[0]))) {
                    pos[0]++;
                }
                if (pos[0] >= json.length()) {
                    break;
                }

                String value;
                if (json.charAt(pos[0]) == '"') {
                    value = readJsonString(json, pos);
                } else {
                    int start = pos[0];
                    while (pos[0] < json.length() && ",}\n".indexOf(json.charAt(pos[0])) == -1) {
                        pos[0]++;
                    }
                    value = json.substring(start, pos[0]).trim();
                }

                if (key.equals("messageID")) {
                    id = value;
                } else if (key.equals("messageNumber")) {
                    try {
                        number = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        number = 0;
                    }
                } else if (key.equals("recipient")) {
                    recipient = value;
                } else if (key.equals("message")) {
                    text = value;
                }
            }
            pos[0]++;

            // Skip anything that is empty or already loaded
            if (id.length() >= 2 && !text.trim().isEmpty() && !messageIDs.contains(id)) {
                addMessage(new Message(id, number, recipient, text), STORE);
                loaded++;
            }
        }
        return loaded;
    }

    // Reads one JSON string starting at the opening quote and moves pos past the closing quote
    private static String readJsonString(String json, int[] pos) {
        StringBuilder result = new StringBuilder();
        int i = pos[0] + 1;
        while (i < json.length() && json.charAt(i) != '"') {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                i++;
                char next = json.charAt(i);
                if (next == 'n') {
                    result.append('\n');
                } else if (next == 'r') {
                    result.append('\r');
                } else if (next == 't') {
                    result.append('\t');
                } else if (next == 'u' && i + 4 < json.length()) {
                    result.append((char) Integer.parseInt(json.substring(i + 1, i + 5), 16));
                    i += 4;
                } else {
                    result.append(next);
                }
            } else {
                result.append(c);
            }
            i++;
        }
        pos[0] = i + 1;
        return result.toString();
    }

    // Getters
    public String getMessageID() { return messageID; }
    public int getMessageNumber() { return messageNumber; }
    public String getRecipient() { return recipient; }
    public String getText() { return text; }
    public String getMessageHash() { return messageHash; }

    public static ArrayList<Message> getSentMessages() { return sentMessages; }
    public static ArrayList<Message> getStoredMessages() { return storedMessages; }
    public static ArrayList<Message> getDisregardedMessages() { return disregardedMessages; }
    public static ArrayList<String> getMessageHashes() { return messageHashes; }
    public static ArrayList<String> getMessageIDs() { return messageIDs; }

    // Used by the tests so they start clean and don't write into the real file
    public static void resetForTesting(String testFile) {
        sentMessages.clear();
        storedMessages.clear();
        disregardedMessages.clear();
        messageHashes.clear();
        messageIDs.clear();
        totalMessagesSent = 0;
        storageFile = testFile;
    }
}
