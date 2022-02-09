package se.tink.backend.aggregation.agents.exceptions.connectivity;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.ProviderErrors.LICENSED_PARTY_REJECTED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.ProviderErrors.PROVIDER_UNAVAILABLE;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.ProviderErrors.UNRECOGNIZED;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@RunWith(JUnitParamsRunner.class)
public class ProviderErrorDefaultMessageMapperTest {

    @Test
    @Parameters
    public void shouldProvideUserMessage(
            ConnectivityErrorDetails.ProviderErrors reason, String expected) {
        // given
        ProviderErrorDefaultMessageMapper provider = new ProviderErrorDefaultMessageMapper();

        // when
        LocalizableKey result = provider.map(reason);

        // then
        assertThat(result).isEqualTo(new LocalizableKey(expected));
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldProvideUserMessage() {
        return new Object[] {
            new Object[] {
                PROVIDER_UNAVAILABLE,
                "A temporary problem has occurred with your bank. Please retry later."
            },
            new Object[] {
                LICENSED_PARTY_REJECTED, "A temporary problem has occurred. Please retry later."
            },
            new Object[] {UNRECOGNIZED, "A temporary problem has occurred. Please retry later."},
        };
    }
}
