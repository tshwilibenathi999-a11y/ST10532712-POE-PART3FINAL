package com.mycompany.main;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

// Part 2 tests: creating and sending messages
public class MessageTest {

    @TempDir
    Path tempFolder;

    // Test data from the PoE
    private Message message1;
    private Message message2;

    @BeforeEach
    public void setUp() {
        Message.resetForTesting(tempFolder.resolve("messages.json").toString());
        message1 = new Message("0012345678", 0, "+27718693002", "Hi Mike, can you join us for dinner tonight?");
        message2 = new Message("0087654321", 1, "08575975889", "Hi Keegan, did you receive the payment?");
    }

    // ---- message length ----

    @Test
    public void messageOf250CharactersIsAccepted() {
        assertEquals("Message ready to send.", Message.checkMessageLength("a".repeat(250)));
    }

    @Test
    public void messageOver250CharactersReportsHowManyTooMany() {
        assertEquals("Message exceeds 250 characters by 10; please reduce the size.",
                Message.checkMessageLength("a".repeat(260)));
    }

    // ---- recipient number ----

    @Test
    public void recipientCorrectlyFormatted() {
        assertEquals("Cell phone number successfully captured.", message1.checkRecipientCell());
    }

    @Test
    public void recipientIncorrectlyFormatted() {
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. "
                + "Please correct the number and try again.", message2.checkRecipientCell());
    }

    // ---- message ID ----

    @Test
    public void generatedMessageIdIsTenDigits() {
        String id = Message.generateMessageID();
        assertEquals(10, id.length());
        assertTrue(id.matches("[0-9]{10}"));
    }

    @Test
    public void messageIdOfTenCharactersIsValid() {
        assertTrue(message1.checkMessageID());
    }

    @Test
    public void messageIdLongerThanTenCharactersIsInvalid() {
        Message tooLong = new Message("12345678901", 0, "+27718693002", "Hello there");
        assertFalse(tooLong.checkMessageID());
    }

    @Test
    public void messageIdMessage() {
        assertEquals("Message ID generated: 0012345678", message1.getMessageIDMessage());
    }

    // ---- message hash ----

    @Test
    public void messageHashForTestMessage1() {
        assertEquals("00:0:HITONIGHT", message1.createMessageHash());
        assertEquals("00:0:HITONIGHT", message1.getMessageHash());
    }

    @Test
    public void messageHashForTestMessage2() {
        assertEquals("00:1:HIPAYMENT", message2.createMessageHash());
    }

    @Test
    public void messageHashesInALoop() {
        String[] ids = {"0012345678", "0087654321", "4512345678"};
        String[] texts = {"Hi Mike, can you join us for dinner tonight?",
            "Hi Keegan, did you receive the payment?", "Thanks"};
        String[] expected = {"00:0:HITONIGHT", "00:1:HIPAYMENT", "45:2:THANKSTHANKS"};

        for (int i = 0; i < texts.length; i++) {
            Message message = new Message(ids[i], i, "+27718693002", texts[i]);
            assertEquals(expected[i], message.createMessageHash());
        }
    }

    // ---- send, store or disregard ----

    @Test
    public void sendMessage() {
        assertEquals("Message successfully sent.", message1.sentMessage(Message.SEND));
        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    public void disregardMessage() {
        assertEquals("Press 0 to delete the message.", message2.sentMessage(Message.DISREGARD));
        assertEquals(0, Message.returnTotalMessages());
    }

    @Test
    public void storeMessageWritesJsonFile() throws Exception {
        assertEquals("Message successfully stored.", message1.sentMessage(Message.STORE));
        String file = Files.readString(tempFolder.resolve("messages.json"));
        assertTrue(file.contains("\"messageID\": \"0012345678\""));
        assertTrue(file.contains("\"message\": \"Hi Mike, can you join us for dinner tonight?\""));
    }

    @Test
    public void storeMessageFailsWhenFileCannotBeWritten() {
        Message.resetForTesting(tempFolder.resolve("missing-folder/messages.json").toString());
        assertEquals("Message could not be stored.", message1.sentMessage(Message.STORE));
        assertEquals(0, Message.getStoredMessages().size());
        assertEquals(0, Message.getMessageHashes().size());
    }

    @Test
    public void invalidChoiceIsRejected() {
        assertEquals("Invalid option.", message1.sentMessage(9));
    }

    // ---- totals and printing ----

    @Test
    public void totalMessagesUsingThePoeTestData() {
        message1.sentMessage(Message.SEND);
        message2.sentMessage(Message.DISREGARD);
        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    public void detailsAreInThePoeOrder() {
        String expected = "Message ID: 0012345678\n"
                + "Message Hash: 00:0:HITONIGHT\n"
                + "Recipient: +27718693002\n"
                + "Message: Hi Mike, can you join us for dinner tonight?\n";
        assertEquals(expected, message1.getDetails());
    }

    @Test
    public void printMessagesListsEverythingSent() {
        message1.sentMessage(Message.SEND);
        assertEquals(message1.getDetails() + "\n", Message.printMessages());
    }

    @Test
    public void printMessagesWhenNothingHasBeenSent() {
        assertEquals("No messages have been sent yet.", Message.printMessages());
    }
}
