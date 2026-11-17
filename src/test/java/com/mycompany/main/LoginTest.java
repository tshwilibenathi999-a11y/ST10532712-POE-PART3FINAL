package com.mycompany.main;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Part 1 tests: registration and login
public class LoginTest {

    private Login login;

    @BeforeEach
    public void setUp() {
        login = new Login("Kyle", "Smith");
    }

    // ---- assertTrue / assertFalse ----

    @Test
    public void usernameCorrectlyFormatted() {
        assertTrue(login.checkUserName("kyl_1"));
    }

    @Test
    public void usernameIncorrectlyFormatted() {
        assertFalse(login.checkUserName("kyle!!!!!!!"));
    }

    @Test
    public void usernameWithoutUnderscoreIsRejected() {
        assertFalse(login.checkUserName("kyle1"));
    }

    @Test
    public void usernameLongerThanFiveIsRejected() {
        assertFalse(login.checkUserName("ky_le1"));
    }

    @Test
    public void passwordMeetsComplexityRules() {
        assertTrue(login.checkPasswordComplexity("Ch&&sec@ke99!"));
    }

    @Test
    public void passwordDoesNotMeetComplexityRules() {
        assertFalse(login.checkPasswordComplexity("password"));
    }

    @Test
    public void passwordMissingCapitalNumberOrSpecialIsRejected() {
        assertFalse(login.checkPasswordComplexity("password1!"));
        assertFalse(login.checkPasswordComplexity("Password!!"));
        assertFalse(login.checkPasswordComplexity("Password11"));
        assertFalse(login.checkPasswordComplexity("Pa1!"));
    }

    @Test
    public void cellPhoneCorrectlyFormatted() {
        assertTrue(Login.checkCellPhoneNumber("+27838968976"));
    }

    @Test
    public void cellPhoneIncorrectlyFormatted() {
        assertFalse(Login.checkCellPhoneNumber("08966553"));
    }

    @Test
    public void cellPhoneWithLettersIsRejected() {
        assertFalse(Login.checkCellPhoneNumber("+2783abc8976"));
    }

    @Test
    public void nullValuesAreRejected() {
        assertFalse(login.checkUserName(null));
        assertFalse(login.checkPasswordComplexity(null));
        assertFalse(Login.checkCellPhoneNumber(null));
    }

    // ---- assertEquals: messages ----

    @Test
    public void usernameMessages() {
        assertEquals("Username successfully captured.", login.usernameStatus("kyl_1"));
        assertEquals("Username is not correctly formatted; please ensure that your username contains "
                + "an underscore and is no more than five characters in length.",
                login.usernameStatus("kyle!!!!!!!"));
    }

    @Test
    public void passwordMessages() {
        assertEquals("Password successfully captured.", login.passwordStatus("Ch&&sec@ke99!"));
        assertEquals("Password is not correctly formatted; please ensure that the password contains "
                + "at least eight characters, a capital letter, a number, and a special character.",
                login.passwordStatus("password"));
    }

    @Test
    public void cellPhoneMessages() {
        assertEquals("Cell phone number successfully added.", login.cellPhoneStatus("+27838968976"));
        assertEquals("Cell phone number incorrectly formatted or does not contain international code.",
                login.cellPhoneStatus("08966553"));
    }

    @Test
    public void registerUserReturnsSuccessMessage() {
        assertEquals("User has been registered successfully.",
                login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976"));
    }

    @Test
    public void registerUserReturnsFirstProblemFound() {
        assertEquals(Login.USERNAME_BAD, login.registerUser("kyle!!!!!!!", "Ch&&sec@ke99!", "+27838968976"));
        assertEquals(Login.PASSWORD_BAD, login.registerUser("kyl_1", "password", "+27838968976"));
        assertEquals(Login.CELL_BAD, login.registerUser("kyl_1", "Ch&&sec@ke99!", "08966553"));
    }

    @Test
    public void failedRegistrationSavesNothing() {
        login.registerUser("kyl_1", "password", "+27838968976");
        assertFalse(login.loginUser("kyl_1", "password"));
    }

    // ---- login ----

    @Test
    public void loginSuccessful() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertTrue(login.loginUser("kyl_1", "Ch&&sec@ke99!"));
    }

    @Test
    public void loginFailedWithWrongPassword() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertFalse(login.loginUser("kyl_1", "wrongPass1!"));
    }

    @Test
    public void loginFailedWithWrongUsername() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertFalse(login.loginUser("kyl_2", "Ch&&sec@ke99!"));
    }

    @Test
    public void loginFailsBeforeAnyoneHasRegistered() {
        assertFalse(login.loginUser("kyl_1", "Ch&&sec@ke99!"));
    }

    @Test
    public void loginStatusMessages() {
        assertEquals("Welcome Kyle, Smith it is great to see you again.", login.returnLoginStatus(true));
        assertEquals("Username or password incorrect, please try again.", login.returnLoginStatus(false));
    }

    @Test
    public void registeredUsernameIsAvailableAfterRegistering() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertEquals("kyl_1", login.getRegisteredUsername());
    }
}
