package se.tink.backend.aggregation.agents.exceptions.connectivity;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.AccountInformationErrors.NOT_ENOUGH_DATA_TO_PROVIDE_PRODUCT;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.AccountInformationErrors.NO_ACCOUNTS;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.AccountInformationErrors.UNRECOGNIZED;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@RunWith(JUnitParamsRunner.class)
public class AccountInformationErrorDefaultMessageProviderTest {

    @Test
    @Parameters
    public void shouldProvideUserMessage(
            ConnectivityErrorDetails.AccountInformationErrors reason, String expected) {
        // given
        AccountInformationErrorDefaultMessageMapper provider =
                new AccountInformationErrorDefaultMessageMapper();

        // when
        LocalizableKey result = provider.map(reason);

        // then
        assertThat(result).isEqualTo(new LocalizableKey(expected));
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldProvideUserMessage() {
        return new Object[] {
            new Object[] {NO_ACCOUNTS, "Your bank account information is not available."},
            new Object[] {
                NOT_ENOUGH_DATA_TO_PROVIDE_PRODUCT,
                "There is not enough banking data to display your results."
            },
            new Object[] {UNRECOGNIZED, "Your bank account information is not available."},
        };
    }
}
