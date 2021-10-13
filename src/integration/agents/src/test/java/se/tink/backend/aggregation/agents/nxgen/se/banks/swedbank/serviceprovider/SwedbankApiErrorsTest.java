package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.Url.INIT_BANKID;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.Url.PROFILE;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwedbankApiErrorsTest {

    @Test
    public void shouldReturnTrueIfAppIsTooOld() {
        HttpResponseException httpResponse = setupHttpResponseMockError(401, APP_TOO_OLD, null);

        boolean result = SwedbankApiErrors.isAppTooOld(httpResponse);

        Assert.assertTrue(result);
    }

    @Test
    public void shouldReturnFalseIfCodeNot401() {
        HttpResponseException httpResponse = setupHttpResponseMockError(500, APP_TOO_OLD, null);

        boolean result = SwedbankApiErrors.isAppTooOld(httpResponse);

        Assert.assertFalse(result);
    }

    @Test
    public void shouldReturnFalseIfAppIsNotTooOld() {
        HttpResponseException httpResponse = setupHttpResponseMockError(500, REQUEST_LIMIT, null);

        boolean result = SwedbankApiErrors.isAppTooOld(httpResponse);

        Assert.assertFalse(result);
    }

    @Test
    public void shouldThrowNoValidCodeIfTokenFormatIsInvalid() {
        HttpResponseException httpResponse =
                setupHttpResponseMockError(400, TOKEN_FORMAT_INVALID, null);

        Throwable throwable =
                catchThrowable(() -> SwedbankApiErrors.handleTokenErrors(httpResponse));

        assertThat(throwable).isExactlyInstanceOf(SupplementalInfoException.class);
        assertEquals(
                "You have not entered a valid code. Please try again",
                (((SupplementalInfoException) throwable).getUserMessage().get()));
    }

    @Test
    public void shouldThrowNoValidCodeIfTokenIsTooOld() {
        HttpResponseException httpResponse = setupHttpResponseMockError(405, TOKEN_TOO_OLD, null);

        Throwable throwable =
                catchThrowable(() -> SwedbankApiErrors.handleTokenErrors(httpResponse));

        assertThat(throwable).isExactlyInstanceOf(SupplementalInfoException.class);
        assertEquals(
                "You have not entered a valid code. Please try again",
                (((SupplementalInfoException) throwable).getUserMessage().get()));
    }

    @Test
    public void shouldThrowNoValidCodeIfTokenIsInvalid() {
        HttpResponseException httpResponse = setupHttpResponseMockError(401, TOKEN_INVALID, null);

        Throwable throwable =
                catchThrowable(() -> SwedbankApiErrors.handleTokenErrors(httpResponse));

        assertThat(throwable).isExactlyInstanceOf(SupplementalInfoException.class);
        assertEquals(
                "You have not entered a valid code. Please try again",
                (((SupplementalInfoException) throwable).getUserMessage().get()));
    }

    @Test
    public void shouldReturnFalseIf404AndRandomBody() {
        HttpResponseException httpResponse = setupHttpResponseMockError(404, REQUEST_LIMIT, null);

        boolean result = SwedbankApiErrors.isUserNotACustomer(httpResponse);

        Assert.assertFalse(result);
    }

    @Test
    public void shouldReturnTrueIfIsNotACustomer() {
        HttpResponseException httpResponse =
                setupHttpResponseMockError(404, CUSTOMER_NOT_FOUND, null);

        boolean result = SwedbankApiErrors.isUserNotACustomer(httpResponse);

        Assert.assertTrue(result);
    }

    @Test
    public void shouldReturnTrueIfAccountNumberIsInvalid() {
        HttpResponseException httpResponse =
                setupHttpResponseMockError(400, ACCOUNTNUMBER_INVALID, null);

        boolean result = SwedbankApiErrors.isAccountNumberInvalid(httpResponse);

        Assert.assertTrue(result);
    }

    @Test
    public void shouldReturnFalseIfBadRequestButNotAccoountNumberInvalidError() {
        HttpResponseException httpResponse =
                setupHttpResponseMockError(400, CUSTOMER_NOT_FOUND, null);

        boolean result = SwedbankApiErrors.isAccountNumberInvalid(httpResponse);

        Assert.assertFalse(result);
    }

    @Test
    public void shouldReturnFalseIfURLIsIdentification() throws URISyntaxException {
        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpRequest spyRequest = spy(httpRequest);
        URI uri = new URI(INIT_BANKID.toString());
        doReturn(uri).when(spyRequest).getURI();
        HttpResponseException httpResponse =
                setupHttpResponseMockError(400, CUSTOMER_NOT_FOUND, spyRequest);
        boolean result = SwedbankApiErrors.isSessionTerminated(httpResponse);

        Assert.assertFalse(result);
    }

    @Test
    public void shouldReturnFalseIfStatus401() throws URISyntaxException {
        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpRequest spyRequest = spy(httpRequest);
        URI uri = new URI(PROFILE.toString());
        doReturn(uri).when(spyRequest).getURI();
        HttpResponseException httpResponse =
                setupHttpResponseMockError(401, CUSTOMER_NOT_FOUND, spyRequest);

        boolean result = SwedbankApiErrors.isSessionTerminated(httpResponse);

        Assert.assertFalse(result);
    }

    @Test
    public void shouldReturnTrueStrongerAuthError() throws URISyntaxException {
        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpRequest spyRequest = spy(httpRequest);
        URI uri = new URI(PROFILE.toString());
        doReturn(uri).when(spyRequest).getURI();
        HttpResponseException httpResponse =
                setupHttpResponseMockError(401, STRONGER_AUTH, spyRequest);

        boolean result = SwedbankApiErrors.isSessionTerminated(httpResponse);

        Assert.assertTrue(result);
    }

    @Test
    public void shouldThrowTransferExecutionExceptionIfPaymentExists() {
        HttpResponseException httpResponse =
                setupHttpResponseMockError(400, PAYMENT_ALREADY_EXISTS, null);

        Throwable throwable =
                catchThrowable(
                        () ->
                                SwedbankApiErrors.throwIfPaymentOrTransferAlreadyExists(
                                        httpResponse));

        assertThat(throwable).isExactlyInstanceOf(TransferExecutionException.class);
        assertEquals(
                "The payment could not be made because an identical payment is already registered",
                (((TransferExecutionException) throwable).getUserMessage()));
    }

    @Test
    public void shouldThrowHttpResponseExceptionIfStatus401() {
        HttpResponseException httpResponse =
                setupHttpResponseMockError(401, PAYMENT_ALREADY_EXISTS, null);

        Throwable throwable =
                catchThrowable(
                        () ->
                                SwedbankApiErrors.throwIfPaymentOrTransferAlreadyExists(
                                        httpResponse));

        assertThat(throwable).isExactlyInstanceOf(HttpResponseException.class);
    }

    private HttpResponseException setupHttpResponseMockError(
            int code, String data, HttpRequest httpRequest) {
        HttpResponse httpResponse = mock(HttpResponse.class);
        HttpResponse spyResponse = spy(httpResponse);
        doReturn(code).when(spyResponse).getStatus();

        ErrorResponse ers = SerializationUtils.deserializeFromString(data, ErrorResponse.class);
        doReturn(ers).when(spyResponse).getBody(any());

        return new HttpResponseException(httpRequest, spyResponse);
    }

    private static final String APP_TOO_OLD =
            "{"
                    + "\"errorMessages\":{"
                    + "\"general\":[{"
                    + "\"code\":\"AUTHORIZATION_FAILED\","
                    + "\"message\":\"Appen behöver uppdateras.\""
                    + "}"
                    + "]"
                    + "}"
                    + "}";

    private static final String REQUEST_LIMIT =
            "{"
                    + "\"errorMessages\":{"
                    + "\"general\":[{"
                    + "\"code\":\"AUTHORIZATION_FAILED\","
                    + "\"message\":\"Reached hour requests limit.\""
                    + "}"
                    + "]"
                    + "}"
                    + "}";

    private static final String TOKEN_FORMAT_INVALID =
            "{"
                    + "\"errorMessages\":{\""
                    + "general\":[],"
                    + "\"fields\":[{"
                    + "\"field\":\"RESPONSE\",\""
                    + "message\":\"Den valda mottagaren tillåter inte betalningar med OCR-nummer, vänligen välj meddelande.\""
                    + "}"
                    + "]"
                    + "}"
                    + "}";

    private static final String TOKEN_TOO_OLD =
            "{"
                    + "\"errorMessages\":{"
                    + "\"general\":[{"
                    + "\"code\":\"NOT_ALLOWED\","
                    + "\"message\":\"Fake message\""
                    + "}"
                    + "]"
                    + "}"
                    + "}";

    private static final String TOKEN_INVALID =
            "{"
                    + "\"errorMessages\":{"
                    + "\"general\":[{"
                    + "\"code\":\"AUTHORIZATION_FAILED\","
                    + "\"message\":\"Fake message\""
                    + "}"
                    + "]"
                    + "}"
                    + "}";

    private static final String CUSTOMER_NOT_FOUND =
            "{"
                    + "\"errorMessages\":{"
                    + "\"general\":[{"
                    + "\"code\":\"NOT_FOUND\","
                    + "\"message\":\"Reached hour requests limit.\""
                    + "}"
                    + "]"
                    + "}"
                    + "}";

    private static final String ACCOUNTNUMBER_INVALID =
            "{"
                    + "\"errorMessages\":{\""
                    + "general\":[],"
                    + "\"fields\":[{"
                    + "\"field\":\"recipientnumber\",\""
                    + "message\":\"Den valda mottagaren tillåter inte betalningar med OCR-nummer, vänligen välj meddelande.\""
                    + "}"
                    + "]"
                    + "}"
                    + "}";

    private static final String STRONGER_AUTH =
            "{"
                    + "\"errorMessages\":{"
                    + "\"general\":[{"
                    + "\"code\":\"STRONGER_AUTHENTICATION_NEEDED\","
                    + "\"message\":\"Reached hour requests limit.\""
                    + "}"
                    + "]"
                    + "}"
                    + "}";

    private static final String PAYMENT_ALREADY_EXISTS =
            "{"
                    + "\"errorMessages\":{"
                    + "\"general\":[{"
                    + "\"code\":\"PAYMENT_ALREADY_EXISTS\","
                    + "\"message\":\"Reached hour requests limit.\""
                    + "}"
                    + "]"
                    + "}"
                    + "}";
}
