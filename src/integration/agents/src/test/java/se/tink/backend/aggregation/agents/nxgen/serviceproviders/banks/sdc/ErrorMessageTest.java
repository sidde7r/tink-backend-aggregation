package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ErrorMessageTest {
    private static final String WRONG_USER_PASSWORD_ERROR = "Login denied.";
    private static final String USER_BLOCKED_ERROR = "The password is blocked";
    private static final String INCORRECT_USER_ID_PASSWORD =
            "X-SDC-ERROR-MESSAGE: Incorrect user ID or password.";

    @Test
    public void userBlocked() {
        assertTrue(SdcConstants.ErrorMessage.PASSWORD_BLOCKED.isBlocked(USER_BLOCKED_ERROR));
        assertTrue(SdcConstants.ErrorMessage.PASSWORD_BLOCKED.isLoginError(USER_BLOCKED_ERROR));
    }

    @Test
    public void userNotBlocked() {
        assertFalse(SdcConstants.ErrorMessage.PASSWORD_BLOCKED.isBlocked(""));
        assertFalse(SdcConstants.ErrorMessage.PASSWORD_BLOCKED.isLoginError(""));
    }

    @Test
    public void wrongUserPassword() {
        assertTrue(SdcConstants.ErrorMessage.LOGIN_DENIED.isLoginError(WRONG_USER_PASSWORD_ERROR));
        assertFalse(SdcConstants.ErrorMessage.LOGIN_DENIED.isBlocked(WRONG_USER_PASSWORD_ERROR));
    }

    @Test
    public void notWrongUserPassword() {
        assertFalse(SdcConstants.ErrorMessage.LOGIN_DENIED.isLoginError(""));
        assertFalse(SdcConstants.ErrorMessage.LOGIN_DENIED.isBlocked(""));
    }

    @Test
    public void incorrectUserPasswordLoginError() {
        assertFalse(
                SdcConstants.ErrorMessage.LOGIN_DENIED.isLoginError(INCORRECT_USER_ID_PASSWORD));
        assertTrue(
                SdcConstants.ErrorMessage.INCORRECT_USER_PASSWORD.isLoginError(
                        INCORRECT_USER_ID_PASSWORD));
        assertFalse(
                SdcConstants.ErrorMessage.PASSWORD_BLOCKED.isLoginError(
                        INCORRECT_USER_ID_PASSWORD));
    }

    @Test
    public void incorrectUserPasswordBlocked() {
        assertFalse(SdcConstants.ErrorMessage.LOGIN_DENIED.isBlocked(INCORRECT_USER_ID_PASSWORD));
        assertFalse(
                SdcConstants.ErrorMessage.INCORRECT_USER_PASSWORD.isBlocked(
                        INCORRECT_USER_ID_PASSWORD));
        assertFalse(
                SdcConstants.ErrorMessage.PASSWORD_BLOCKED.isBlocked(INCORRECT_USER_ID_PASSWORD));
    }
}
