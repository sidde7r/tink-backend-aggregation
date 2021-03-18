package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(JUnitParamsRunner.class)
public class ErrorCheckerTest {

    private final HttpResponse httpResponse = mock(HttpResponse.class);
    private final HttpResponseException httpResponseException = mock(HttpResponseException.class);

    @Test
    public void throwErrorWithCorrectMessageNoAccount() {

        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getBody(String.class))
                .thenReturn(FiduciaConstants.ErrorMessageKeys.NO_ACCOUNT_AVAILABLE);

        AuthorizationException authorizationException =
                (AuthorizationException) ErrorChecker.errorChecker(httpResponseException);
        assertEquals(
                FiduciaConstants.EndUserErrorMessageKeys.UNAVAILABLE_ACCOUNT_MESSAGE.get(),
                authorizationException.getUserMessage().get());
    }

    @Test
    public void throwSameErrorBackIfNoMatch() {
        String UNMAPPED_ERROR = "UNMAPPED_ERROR";

        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getBody(String.class)).thenReturn(UNMAPPED_ERROR);

        HttpResponseException httpResponseExceptionThrown =
                (HttpResponseException) ErrorChecker.errorChecker(httpResponseException);
        assertEquals(
                UNMAPPED_ERROR, httpResponseExceptionThrown.getResponse().getBody(String.class));
    }

    @Test
    public void throwErrorWithCorrectMessageBankUnavailable() {

        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getBody(String.class))
                .thenReturn(FiduciaConstants.ErrorMessageKeys.ERROR_KONF);

        LoginException loginException =
                (LoginException) ErrorChecker.errorChecker(httpResponseException);
        assertEquals(
                FiduciaConstants.EndUserErrorMessageKeys.BANK_NO_LONGER_AVAILABLE_MESSAGE.get(),
                loginException.getUserMessage().get());
    }
}
