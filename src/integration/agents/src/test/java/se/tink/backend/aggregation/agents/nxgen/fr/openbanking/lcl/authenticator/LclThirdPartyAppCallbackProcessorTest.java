package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.authenticator;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.CallbackParams;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.OAuth2ThirdPartyAppRequestParamsProvider;

public class LclThirdPartyAppCallbackProcessorTest {

    private LclThirdPartyAppCallbackProcessor callbackProcessor;

    @Before
    public void setUp() {
        OAuth2ThirdPartyAppRequestParamsProvider partyAppRequestParamsProvider =
                mock(OAuth2ThirdPartyAppRequestParamsProvider.class);
        callbackProcessor = new LclThirdPartyAppCallbackProcessor(partyAppRequestParamsProvider);
    }

    @Test
    public void shouldThrowAuthorizationException() {
        // given
        String error = null;
        Map<String, String> callbackData = new HashMap<>();
        callbackData.put(CallbackParams.ERROR_DESCRIPTION, "PSU access denied");

        // when
        Throwable thrown =
                catchThrowable(() -> callbackProcessor.processError(error, callbackData));

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("Cause: AuthorizationError.UNAUTHORIZED");
    }

    @Test
    public void shouldThrowBankException() {
        // given
        String error = "server_error";

        // when
        Throwable thrown = catchThrowable(() -> callbackProcessor.processError(error, null));

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(BankServiceException.class)
                .hasMessage("Cause: BankServiceError.BANK_SIDE_FAILURE");
    }
}
