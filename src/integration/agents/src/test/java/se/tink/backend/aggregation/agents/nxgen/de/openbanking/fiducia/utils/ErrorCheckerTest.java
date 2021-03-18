package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class ErrorCheckerTest {

    private final HttpResponse httpResponse = mock(HttpResponse.class);
    private final HttpResponseException httpResponseException = mock(HttpResponseException.class);

    @Test
    public void throwErrorWithCorrectMessageNoAccount() {
        // given
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getBody(String.class))
                .thenReturn(FiduciaConstants.ErrorMessageKeys.NO_ACCOUNT_AVAILABLE);

        // when
        AuthorizationException authorizationException =
                (AuthorizationException) ErrorChecker.errorChecker(httpResponseException);

        // then
        assertEquals(
                FiduciaConstants.EndUserErrorMessageKeys.UNAVAILABLE_ACCOUNT_MESSAGE.get(),
                authorizationException.getUserMessage().get());
    }

    @Test
    public void throwSameErrorBackIfNoMatch() {
        // given
        String UNMAPPED_ERROR = "UNMAPPED_ERROR";
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getBody(String.class)).thenReturn(UNMAPPED_ERROR);

        // when
        HttpResponseException httpResponseExceptionThrown =
                (HttpResponseException) ErrorChecker.errorChecker(httpResponseException);

        // then
        assertEquals(
                UNMAPPED_ERROR, httpResponseExceptionThrown.getResponse().getBody(String.class));
    }

    @Test
    public void throwErrorWithCorrectMessageBankUnavailable() {
        // given
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getBody(String.class))
                .thenReturn(FiduciaConstants.ErrorMessageKeys.ERROR_KONF);

        // when
        LoginException loginException =
                (LoginException) ErrorChecker.errorChecker(httpResponseException);

        // then
        assertEquals(
                FiduciaConstants.EndUserErrorMessageKeys.BANK_NO_LONGER_AVAILABLE_MESSAGE.get(),
                loginException.getUserMessage().get());
    }

    @Test
    public void throwBackErrorWithNullMessageIfResponseHasNoBody() {
        // given
        when(httpResponseException.getResponse()).thenReturn(httpResponse);

        // when
        HttpResponseException httpResponseExceptionThrown =
                (HttpResponseException) ErrorChecker.errorChecker(httpResponseException);

        // then
        assertNull(httpResponseExceptionThrown.getMessage());
    }
}
