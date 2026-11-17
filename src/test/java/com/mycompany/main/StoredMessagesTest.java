package com.mycompany.main;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

// Part 3 tests: arrays, searching, deleting, reports and reading the JSON file
public class StoredMessagesTest {

    @TempDir
    Path tempFolder;

    // Test data from the PoE (messages 1 to 5)
    private Message message1;
    private Message message2;
    private Message message3;
    private Message message4;
    private Message message5;

    // Empty lists, a temporary JSON file and the five test messages before every test
    @BeforeEach
    public void setUp() {
        Message.resetForTesting(tempFolder.resolve("messages.json").toString());

        message1 = new Message("1000000001", 0, "+27834557896", "Did you get the cake?");
        message2 = new Message("1000000002", 1, "+27838884567",
                "Where are you? You are late! I have asked you to be on time.");
        message3 = new Message("1000000003", 2, "+27834484567", "Yohoooo, I am at your gate.");
        // The PoE gives message 4 a developer number and searches for it by "0838884567"
        message4 = new Message("0838884567", 3, "0838884567", "It is dinner time!");
        message5 = new Message("1000000005", 4, "+27838884567", "Ok, I am leaving without you.");
    }

    // Fills the arrays with the flags given in the PoE
    private void loadTestData() {
        Message.addMessage(message1, Message.SEND);
        Message.addMessage(message2, Message.STORE);
        Message.addMessage(message3, Message.DISREGARD);
        Message.addMessage(message4, Message.SEND);
        Message.addMessage(message5, Message.STORE);
    }

    // ---- arrays ----

    @Test
    public void sentMessagesArrayCorrectlyPopulated() {
        loadTestData();
        ArrayList<Message> sent = Message.getSentMessages();
        assertEquals(2, sent.size());
        assertEquals("Did you get the cake?", sent.get(0).getText());
        assertEquals("It is dinner time!", sent.get(1).getText());
        assertEquals(2, Message.returnTotalMessages());
    }

    @Test
    public void storedAndDisregardedArraysCorrectlyPopulated() {
        loadTestData();
        assertEquals(2, Message.getStoredMessages().size());
        assertEquals(message2.getText(), Message.getStoredMessages().get(0).getText());
        assertEquals(message5.getText(), Message.getStoredMessages().get(1).getText());
        assertEquals(1, Message.getDisregardedMessages().size());
        assertEquals("Yohoooo, I am at your gate.", Message.getDisregardedMessages().get(0).getText());
    }

    @Test
    public void hashAndIdArraysHoldEveryMessage() {
        loadTestData();
        assertEquals(5, Message.getMessageHashes().size());
        assertEquals(5, Message.getMessageIDs().size());
        assertEquals(message1.getMessageHash(), Message.getMessageHashes().get(0));
        assertEquals("1000000005", Message.getMessageIDs().get(4));
    }

    @Test
    public void disregardMessageAddsToDisregardedArray() {
        message3.disregardMessage();
        assertEquals(1, Message.getDisregardedMessages().size());
        assertEquals(0, Message.returnTotalMessages());
    }

    // ---- longest message ----

    @Test
    public void displayLongestStoredMessage() {
        loadTestData();
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                Message.getLongestStoredMessage());
    }

    @Test
    public void longestMessageWhenNothingIsStored() {
        assertEquals("There are no stored messages.", Message.getLongestStoredMessage());
    }

    // ---- searching ----

    @Test
    public void searchForMessageID() {
        loadTestData();
        Message found = Message.searchByMessageID("0838884567");
        assertNotNull(found);
        assertEquals("It is dinner time!", found.getText());
        assertEquals("0838884567", found.getRecipient());
    }

    @Test
    public void searchForMessageIDThatDoesNotExist() {
        loadTestData();
        assertNull(Message.searchByMessageID("9999999999"));
    }

    @Test
    public void searchAllMessagesForOneRecipient() {
        loadTestData();
        ArrayList<String> found = Message.searchByRecipient("+27838884567");
        assertEquals(2, found.size());
        assertEquals("Where are you? You are late! I have asked you to be on time.", found.get(0));
        assertEquals("Ok, I am leaving without you.", found.get(1));
    }

    @Test
    public void searchForRecipientWithNoMessages() {
        loadTestData();
        assertTrue(Message.searchByRecipient("+27000000000").isEmpty());
    }

    // ---- deleting ----

    @Test
    public void deleteMessageUsingHash() {
        loadTestData();
        String result = Message.deleteByHash(message2.getMessageHash());
        assertEquals("Message: \"Where are you? You are late! I have asked you to be on time.\" successfully deleted.",
                result);
        assertEquals(1, Message.getStoredMessages().size());
        assertFalse(Message.getMessageHashes().contains(message2.getMessageHash()));
        assertFalse(Message.getMessageIDs().contains("1000000002"));
    }

    @Test
    public void deleteMessageWithUnknownHash() {
        loadTestData();
        assertEquals("No message was found with that hash.", Message.deleteByHash("00:0:NOPE"));
        assertEquals(2, Message.getStoredMessages().size());
    }

    @Test
    public void deletingAStoredMessageUpdatesTheJsonFile() {
        message2.sentMessage(Message.STORE);
        message5.sentMessage(Message.STORE);
        Message.deleteByHash(message2.getMessageHash());

        Message.resetForTesting(tempFolder.resolve("messages.json").toString());
        assertEquals(1, Message.loadStoredMessages());
        assertEquals("Ok, I am leaving without you.", Message.getStoredMessages().get(0).getText());
    }

    // ---- sender and recipient list ----

    @Test
    public void displaySenderAndRecipientOfStoredMessages() {
        loadTestData();
        String expected = "Sender: kyl_1 | Recipient: +27838884567\n"
                + "Sender: kyl_1 | Recipient: +27838884567\n";
        assertEquals(expected, Message.displayStoredSendersAndRecipients("kyl_1"));
    }

    @Test
    public void senderAndRecipientListWhenNothingIsStored() {
        assertEquals("There are no stored messages.", Message.displayStoredSendersAndRecipients("kyl_1"));
    }

    // ---- reports ----

    @Test
    public void sentMessagesReport() {
        loadTestData();
        String expected = "--- Sent messages report ---\n"
                + "1) Message ID: 1000000001\n"
                + "   Message Hash: " + message1.getMessageHash() + "\n"
                + "   Recipient: +27834557896\n"
                + "   Message: Did you get the cake?\n"
                + "2) Message ID: 0838884567\n"
                + "   Message Hash: " + message4.getMessageHash() + "\n"
                + "   Recipient: 0838884567\n"
                + "   Message: It is dinner time!\n";
        assertEquals(expected, Message.getSentReport());
    }

    @Test
    public void storedMessagesReportListsFullDetails() {
        loadTestData();
        String report = Message.getStoredReport();
        assertTrue(report.contains("Message ID: 1000000002"));
        assertTrue(report.contains("Message Hash: " + message2.getMessageHash()));
        assertTrue(report.contains("Recipient: +27838884567"));
        assertTrue(report.contains("Message: Ok, I am leaving without you."));
        assertFalse(report.contains("Did you get the cake?"));
    }

    @Test
    public void reportWhenNothingIsStored() {
        assertEquals("--- Stored messages report ---\nNo messages to show.\n", Message.getStoredReport());
    }

    // ---- reading the JSON file ----

    @Test
    public void storedMessagesAreReadBackFromJsonFile() {
        message2.sentMessage(Message.STORE);
        message5.sentMessage(Message.STORE);

        // Clear the arrays (keeps the same file), then read the file back in
        Message.resetForTesting(tempFolder.resolve("messages.json").toString());
        assertEquals(0, Message.getStoredMessages().size());

        assertEquals(2, Message.loadStoredMessages());
        ArrayList<Message> stored = Message.getStoredMessages();
        assertEquals(message2.getText(), stored.get(0).getText());
        assertEquals(message2.getMessageHash(), stored.get(0).getMessageHash());
        assertEquals("+27838884567", stored.get(1).getRecipient());
        assertEquals("1000000005", stored.get(1).getMessageID());
        assertEquals(2, Message.getMessageIDs().size());
    }

    @Test
    public void loadingTwiceDoesNotCreateDuplicates() {
        message2.sentMessage(Message.STORE);
        Message.resetForTesting(tempFolder.resolve("messages.json").toString());

        assertEquals(1, Message.loadStoredMessages());
        assertEquals(0, Message.loadStoredMessages());
        assertEquals(1, Message.getStoredMessages().size());
    }

    @Test
    public void loadingWhenThereIsNoFileReturnsZero() {
        assertEquals(0, Message.loadStoredMessages());
    }

    @Test
    public void jsonKeepsQuotesBracesAndNewlines() {
        Message tricky = new Message("2000000001", 0, "+27834557896",
                "She said \"go\" {now} \\ back\nslash");
        tricky.sentMessage(Message.STORE);
        Message.resetForTesting(tempFolder.resolve("messages.json").toString());

        assertEquals(1, Message.loadStoredMessages());
        assertEquals("She said \"go\" {now} \\ back\nslash", Message.getStoredMessages().get(0).getText());
    }
}
