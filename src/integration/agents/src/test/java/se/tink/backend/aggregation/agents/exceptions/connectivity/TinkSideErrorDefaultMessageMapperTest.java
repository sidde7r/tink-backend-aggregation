package se.tink.backend.aggregation.agents.exceptions.connectivity;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.TinkSideErrors.AUTHENTICATION_METHOD_NOT_SUPPORTED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.TinkSideErrors.OPERATION_NOT_SUPPORTED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.TinkSideErrors.TIMEOUT;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.TinkSideErrors.TINK_INTERNAL_SERVER_ERROR;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.TinkSideErrors.UNKNOWN_ERROR;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.TinkSideErrors.UNRECOGNIZED;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@RunWith(JUnitParamsRunner.class)
public class TinkSideErrorDefaultMessageMapperTest {

    @Test
    @Parameters
    public void shouldProvideUserMessage(
            ConnectivityErrorDetails.TinkSideErrors reason, String expected) {
        // given
        TinkSideErrorDefaultMessageMapper provider = new TinkSideErrorDefaultMessageMapper();

        // when
        LocalizableKey result = provider.map(reason);

        // then
        assertThat(result).isEqualTo(new LocalizableKey(expected));
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldProvideUserMessage() {
        return new Object[] {
            new Object[] {UNKNOWN_ERROR, "A problem has occurred. Please retry later."},
            new Object[] {
                TINK_INTERNAL_SERVER_ERROR, "A problem has occurred. Please retry later."
            },
            new Object[] {
                OPERATION_NOT_SUPPORTED,
                "You have chosen an invalid authentication method. Please choose another authentication method."
            },
            new Object[] {
                AUTHENTICATION_METHOD_NOT_SUPPORTED,
                "You have chosen an unsupported authentication method. Please go back and choose another authentication method."
            },
            new Object[] {TIMEOUT, "Your connection has timed out. Please retry."},
            new Object[] {UNRECOGNIZED, "A temporary problem has occurred. Please retry later."},
        };
    }
}
