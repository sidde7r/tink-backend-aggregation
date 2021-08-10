package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost.agent.authenticator;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost.authenticator.BPostAuthenticatorHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.CallbackParams;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(Parameterized.class)
public class BPostAuthenticatorHelperTest {

    private final String exceptionMessage;

    private final Class<? extends Exception> exceptionClass;

    private final BPostAuthenticatorHelper bPostAuthenticatorHelper =
            new BPostAuthenticatorHelper(
                    mock(Xs2aDevelopersApiClient.class),
                    mock(PersistentStorage.class),
                    mock(Xs2aDevelopersProviderConfiguration.class),
                    mock(LocalDateTimeSource.class),
                    mock(Credentials.class));

    public BPostAuthenticatorHelperTest(
            String exceptionMessage, Class<? extends Exception> exceptionClass) {
        this.exceptionMessage = exceptionMessage;
        this.exceptionClass = exceptionClass;
    }

    @Parameters
    public static Collection<Object[]> exceptionsData() {
        return asList(
                new Object[][] {
                    {"action_canceled_by_user", ThirdPartyAppException.class},
                    {"An unexpected error occured", BankServiceException.class},
                    {"Validate request already called", BankIdException.class}
                });
    }

    @Test
    public void shouldHandleSpecificCallbackDataError() {

        // given
        Map<String, String> callbackData =
                singletonMap(CallbackParams.ERROR_DESCRIPTION, exceptionMessage);

        // expect
        assertThatThrownBy(
                        () ->
                                bPostAuthenticatorHelper.handleSpecificCallbackDataError(
                                        callbackData))
                .isExactlyInstanceOf(exceptionClass);
    }
}
