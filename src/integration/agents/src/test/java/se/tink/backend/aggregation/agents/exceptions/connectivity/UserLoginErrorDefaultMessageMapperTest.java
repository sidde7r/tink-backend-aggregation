package se.tink.backend.aggregation.agents.exceptions.connectivity;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.UserLoginErrors.DYNAMIC_CREDENTIALS_FLOW_CANCELLED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.UserLoginErrors.DYNAMIC_CREDENTIALS_FLOW_TIMEOUT;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.UserLoginErrors.DYNAMIC_CREDENTIALS_INCORRECT;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.UserLoginErrors.STATIC_CREDENTIALS_INCORRECT;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.UserLoginErrors.THIRD_PARTY_AUTHENTICATION_UNAVAILABLE;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.UserLoginErrors.UNRECOGNIZED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.UserLoginErrors.USER_BLOCKED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.UserLoginErrors.USER_CONCURRENT_LOGINS;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.UserLoginErrors.USER_NOT_A_CUSTOMER;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@RunWith(JUnitParamsRunner.class)
public class UserLoginErrorDefaultMessageMapperTest {

    @Test
    @Parameters
    public void shouldProvideUserMessage(
            ConnectivityErrorDetails.UserLoginErrors reason, String expected) {
        // given
        UserLoginErrorDefaultMessageMapper provider = new UserLoginErrorDefaultMessageMapper();

        // when
        LocalizableKey result = provider.map(reason);

        // then
        assertThat(result).isEqualTo(new LocalizableKey(expected));
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldProvideUserMessage() {
        return new Object[] {
            new Object[] {
                THIRD_PARTY_AUTHENTICATION_UNAVAILABLE,
                "The authentication method can not be used. Please go back and choose another method or retry later."
            },
            new Object[] {
                STATIC_CREDENTIALS_INCORRECT,
                "You have entered the wrong user name or/and password. Please try to log in again."
            },
            new Object[] {
                DYNAMIC_CREDENTIALS_INCORRECT, "Your one-time password is incorrect. Please retry."
            },
            new Object[] {
                DYNAMIC_CREDENTIALS_FLOW_CANCELLED,
                "You have cancelled authentication. Please retry."
            },
            new Object[] {
                DYNAMIC_CREDENTIALS_FLOW_TIMEOUT, "Your connection has timed out. Please retry."
            },
            new Object[] {
                USER_NOT_A_CUSTOMER,
                "You can not log in. The bank you selected does not accept your choice. Please select another bank or contact your bank."
            },
            new Object[] {
                USER_CONCURRENT_LOGINS, "You are already logged in. Please log out and retry."
            },
            new Object[] {
                USER_BLOCKED,
                "You can not log in with your bank. Your account may be blocked from logging in. Please contact your bank."
            },
            new Object[] {UNRECOGNIZED, "A temporary problem has occurred. Please retry later."},
        };
    }
}
