package se.tink.backend.aggregation.agents.exceptions.connectivity;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.AuthorizationErrors.ACTION_NOT_PERMITTED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.AuthorizationErrors.SESSION_EXPIRED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.AuthorizationErrors.UNRECOGNIZED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.AuthorizationErrors.USER_ACTION_REQUIRED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.AuthorizationErrors.USER_ACTION_REQUIRED_UNSIGNED_AGREEMENT;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@RunWith(JUnitParamsRunner.class)
public class AuthorizationErrorDefaultMessageMapperTest {

    @Test
    @Parameters
    public void shouldProvideUserMessage(
            ConnectivityErrorDetails.AuthorizationErrors reason, String expected) {
        // given
        AuthorizationErrorDefaultMessageMapper provider =
                new AuthorizationErrorDefaultMessageMapper();

        // when
        LocalizableKey result = provider.map(reason);

        // then
        assertThat(result).isEqualTo(new LocalizableKey(expected));
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldProvideUserMessage() {
        return new Object[] {
            new Object[] {
                ACTION_NOT_PERMITTED,
                "You are not authorised to use this service. Please contact your bank."
            },
            new Object[] {
                SESSION_EXPIRED,
                "You have been logged out for security reasons. Please log in again."
            },
            new Object[] {
                USER_ACTION_REQUIRED_UNSIGNED_AGREEMENT,
                "You can not log in. Please activate online banking in your bank app or contact your bank to resolve this issue."
            },
            new Object[] {
                USER_ACTION_REQUIRED,
                "You can not log in. Please retry or contact your bank to resolve this issue."
            },
            new Object[] {UNRECOGNIZED, "You are not authorized to use this service."},
        };
    }
}
