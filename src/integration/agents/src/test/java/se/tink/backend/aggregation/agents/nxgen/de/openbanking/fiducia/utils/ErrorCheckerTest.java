package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.payment.DuplicatePaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.EndUserErrorMessageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.ErrorMessageKeys;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(JUnitParamsRunner.class)
public class ErrorCheckerTest {

    private final HttpResponse httpResponse = mock(HttpResponse.class);
    private final HttpResponseException httpResponseException = mock(HttpResponseException.class);

    @Test
    public void throwSameErrorBackIfNoMatch() {
        // given
        String UNMAPPED_ERROR = "UNMAPPED_ERROR";
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getBody(String.class)).thenReturn(UNMAPPED_ERROR);

        // when
        HttpResponseException httpResponseExceptionThrown =
                (HttpResponseException) ErrorChecker.mapError(httpResponseException);

        // then
        assertEquals(
                UNMAPPED_ERROR, httpResponseExceptionThrown.getResponse().getBody(String.class));
    }

    @Parameters(method = "agentsExceptionsToTest")
    public <T extends AgentException> void testAgentErrors(
            String errorMessage, Class<T> agentException, String expectedError) {
        // given
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getBody(String.class)).thenReturn(errorMessage);

        // when
        T mappedException = agentException.cast(ErrorChecker.mapError(httpResponseException));

        // then
        assertEquals(expectedError, mappedException.getUserMessage().get());
    }

    @Parameters(method = "paymentExceptionsToTest")
    public <T extends PaymentException> void testPaymentExceptions(
            String errorMessage, Class<T> paymentException, String expectedError) {
        // given
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getBody(String.class)).thenReturn(errorMessage);

        // when
        T mappedException = paymentException.cast(ErrorChecker.mapError(httpResponseException));

        // then
        assertEquals(expectedError, mappedException.getMessage());
    }

    @SuppressWarnings("unused")
    public Object[] agentExceptionsToTest() {
        return new Object[] {
            new Object[] {
                ErrorMessageKeys.NO_ACCOUNT_AVAILABLE,
                AuthorizationException.class,
                EndUserErrorMessageKeys.UNAVAILABLE_ACCOUNT_MESSAGE.get()
            },
            new Object[] {
                ErrorMessageKeys.ERROR_KONF,
                LoginException.class,
                EndUserErrorMessageKeys.BANK_NO_LONGER_AVAILABLE_MESSAGE.get()
            },
            new Object[] {
                ErrorMessageKeys.NO_ACCOUNT_AVAILABLE,
                AuthorizationException.class,
                EndUserErrorMessageKeys.UNAVAILABLE_ACCOUNT_MESSAGE.get()
            },
            new Object[] {
                ErrorMessageKeys.ORDER_REJECTED,
                LoginException.class,
                EndUserErrorMessageKeys.ORDER_NOT_PROCESSED_MESSAGE.get()
            },
            new Object[] {
                ErrorMessageKeys.ORDER_BLOCKED,
                LoginException.class,
                EndUserErrorMessageKeys.ORDER_NOT_PROCESSED_MESSAGE.get()
            },
            new Object[] {
                "",
                BankServiceException.class,
                "The bank service has temporarily failed; please try again later."
            }
        };
    }

    @SuppressWarnings("unused")
    public Object[] paymentExceptionsToTest() {
        return new Object[] {
            new Object[] {
                ErrorMessageKeys.ORDER_LIMIT_EXCEEDED,
                PaymentRejectedException.class,
                "The number of transactions exceeds the acceptance limit."
            },
            new Object[] {
                ErrorMessageKeys.MISSING_COVERAGE,
                PaymentRejectedException.class,
                "The payment was rejected by the bank."
            },
            new Object[] {
                ErrorMessageKeys.ORDER_DUPLICATED,
                DuplicatePaymentException.class,
                "The payment could not be made because an identical payment is already registered"
            },
            new Object[] {
                ErrorMessageKeys.ORDER_REJECTED,
                DuplicatePaymentException.class,
                "The payment could not be made because an identical payment is already registered"
            },
            new Object[] {
                ErrorMessageKeys.NO_PAYMENT_AUTHORIZATION,
                PaymentAuthorizationException.class,
                "Payment was not authorised. Please try again."
            }
        };
    }
}
