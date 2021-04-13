package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.http.request.HttpMethod.OPTIONS;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbStorage;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class DkbAuthApiClientTest {

    private static final int OK_REQUEST_STATUS = 200;
    private static final int BAD_REQUEST_STATUS = 400;

    private TinkHttpClient clientMock = mock(TinkHttpClient.class);
    private DkbAuthRequestsFactory requestFactoryMock = mock(DkbAuthRequestsFactory.class);
    private DkbStorage storageMock = mock(DkbStorage.class);

    private DkbAuthApiClient tested =
            new DkbAuthApiClient(clientMock, requestFactoryMock, storageMock);

    private static <T> HttpResponse dummyHttpResponse(int status, Class<T> clazz, T body) {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(status);
        when(response.getBody(clazz)).thenReturn(body);
        return response;
    }

    private static HttpRequest dummyHttpRequest(String url) {
        return new HttpRequestImpl(OPTIONS, new URL(url));
    }

    @Test
    public void authenticate1stFactorShouldCallRequestFactoryAndExecuteRequest()
            throws LoginException {
        // given
        String givenUsername = "username";
        String givenPassword = "password";
        HttpRequest givenHttpRequest = dummyHttpRequest("auth1stFactorUrl");
        when(requestFactoryMock.generateAuth1stFactorRequest(givenUsername, givenPassword))
                .thenReturn(givenHttpRequest);

        HttpResponse givenSuccessfulHttpResponse =
                dummyHttpResponse(OK_REQUEST_STATUS, AuthResult.class, new AuthResult());
        when(clientMock.request(HttpResponse.class, givenHttpRequest))
                .thenReturn(givenSuccessfulHttpResponse);

        // when
        tested.authenticate1stFactor(givenUsername, givenPassword);

        // then
        verify(requestFactoryMock).generateAuth1stFactorRequest(givenUsername, givenPassword);
        verify(clientMock).request(HttpResponse.class, givenHttpRequest);
    }

    @Test
    public void select2ndFactorAuthMethodShouldCallRequestFactoryAndExecuteRequest()
            throws LoginException {
        // given
        String givenMethodId = "methodId";
        HttpRequest givenHttpRequest = dummyHttpRequest("select2ndFactorUrl");
        when(requestFactoryMock.generateAuthMethodSelectionRequest(givenMethodId))
                .thenReturn(givenHttpRequest);

        HttpResponse givenSuccessfulHttpResponse =
                dummyHttpResponse(OK_REQUEST_STATUS, AuthResult.class, new AuthResult());
        when(clientMock.request(HttpResponse.class, givenHttpRequest))
                .thenReturn(givenSuccessfulHttpResponse);

        // when
        tested.select2ndFactorAuthMethod(givenMethodId);

        // then
        verify(requestFactoryMock).generateAuthMethodSelectionRequest(givenMethodId);
        verify(clientMock).request(HttpResponse.class, givenHttpRequest);
    }

    @Test
    public void
            select2ndFactorAuthMethodShouldCallRequestFactoryAndExecuteRequestAndThrowException() {
        // given
        String givenMethodId = "methodId";
        HttpRequest givenHttpRequest = dummyHttpRequest("select2ndFactorUrl");
        when(requestFactoryMock.generateAuthMethodSelectionRequest(givenMethodId))
                .thenReturn(givenHttpRequest);

        HttpResponse givenSuccessfulHttpResponse =
                dummyHttpResponse(BAD_REQUEST_STATUS, AuthResult.class, new AuthResult());
        when(clientMock.request(HttpResponse.class, givenHttpRequest))
                .thenReturn(givenSuccessfulHttpResponse);

        // when
        Throwable throwable = catchThrowable(() -> tested.select2ndFactorAuthMethod(givenMethodId));

        // then
        verify(requestFactoryMock).generateAuthMethodSelectionRequest(givenMethodId);
        verify(clientMock).request(HttpResponse.class, givenHttpRequest);

        // and
        assertThat(throwable)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void submit2ndFactorTanCodeShouldCallRequestFactoryAndExecuteRequest()
            throws LoginException {
        // given
        String givenTanCode = "tanCode";
        HttpRequest givenHttpRequest = dummyHttpRequest("summitTanCode");
        when(requestFactoryMock.generateTanSubmissionRequest(givenTanCode))
                .thenReturn(givenHttpRequest);

        HttpResponse givenSuccessfulHttpResponse =
                dummyHttpResponse(OK_REQUEST_STATUS, AuthResult.class, new AuthResult());
        when(clientMock.request(HttpResponse.class, givenHttpRequest))
                .thenReturn(givenSuccessfulHttpResponse);

        // when
        tested.submit2ndFactorTanCode(givenTanCode);

        // then
        verify(requestFactoryMock).generateTanSubmissionRequest(givenTanCode);
        verify(clientMock).request(HttpResponse.class, givenHttpRequest);
    }

    @Test
    public void submit2ndFactorTanCodeShouldCallRequestFactoryAndExecuteRequestAndThrowException() {
        // given
        String givenTanCode = "tanCode";
        HttpRequest givenHttpRequest = dummyHttpRequest("summitTanCode");
        when(requestFactoryMock.generateTanSubmissionRequest(givenTanCode))
                .thenReturn(givenHttpRequest);

        HttpResponse givenSuccessfulHttpResponse =
                dummyHttpResponse(BAD_REQUEST_STATUS, AuthResult.class, new AuthResult());
        when(clientMock.request(HttpResponse.class, givenHttpRequest))
                .thenReturn(givenSuccessfulHttpResponse);

        // when
        Throwable throwable = catchThrowable(() -> tested.submit2ndFactorTanCode(givenTanCode));

        // then
        verify(requestFactoryMock).generateTanSubmissionRequest(givenTanCode);
        verify(clientMock).request(HttpResponse.class, givenHttpRequest);

        // and
        assertThat(throwable)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void createConsentShouldCallRequestFactoryAndExecuteRequest() {
        // given
        LocalDate givenValidUntil = LocalDate.parse("2020-01-02");
        HttpRequest givenHttpRequest = dummyHttpRequest("createConsent");
        when(requestFactoryMock.generateCreateConsentRequest(any())).thenReturn(givenHttpRequest);

        HttpResponse givenSuccessfulHttpResponse =
                dummyHttpResponse(OK_REQUEST_STATUS, ConsentResponse.class, new ConsentResponse());
        when(clientMock.request(HttpResponse.class, givenHttpRequest))
                .thenReturn(givenSuccessfulHttpResponse);

        // when
        tested.createConsent(givenValidUntil);

        // then
        verify(requestFactoryMock).generateCreateConsentRequest(givenValidUntil);
        verify(clientMock).request(HttpResponse.class, givenHttpRequest);
    }

    @Test
    public void getConsentDetailsShouldCallRequestFactoryAndExecuteRequest() {
        // given
        String givenConsentId = "consentId";
        HttpRequest givenHttpRequest = dummyHttpRequest("createConsent");
        when(requestFactoryMock.generateGetConsentRequest(any())).thenReturn(givenHttpRequest);

        HttpResponse givenSuccessfulHttpResponse =
                dummyHttpResponse(
                        OK_REQUEST_STATUS,
                        ConsentDetailsResponse.class,
                        new ConsentDetailsResponse());
        when(clientMock.request(HttpResponse.class, givenHttpRequest))
                .thenReturn(givenSuccessfulHttpResponse);

        // when
        tested.getConsentDetails(givenConsentId);

        // then
        verify(requestFactoryMock).generateGetConsentRequest(givenConsentId);
        verify(clientMock).request(HttpResponse.class, givenHttpRequest);
    }

    @Test
    public void startConsentAuthorizationShouldCallRequestFactoryAndExecuteRequest()
            throws LoginException {
        // given
        String givenConsentId = "consentId";
        HttpRequest givenHttpRequest = dummyHttpRequest("startConsentAuthUrl");
        when(requestFactoryMock.generateConsentAuthorizationRequest(givenConsentId))
                .thenReturn(givenHttpRequest);

        HttpResponse givenSuccessfulHttpResponse =
                dummyHttpResponse(
                        OK_REQUEST_STATUS, ConsentAuthorization.class, new ConsentAuthorization());
        when(clientMock.request(HttpResponse.class, givenHttpRequest))
                .thenReturn(givenSuccessfulHttpResponse);

        // when
        tested.startConsentAuthorization(givenConsentId);

        // then
        verify(requestFactoryMock).generateConsentAuthorizationRequest(givenConsentId);
        verify(clientMock).request(HttpResponse.class, givenHttpRequest);
    }

    @Test
    public void
            startConsentAuthorizationShouldCallRequestFactoryAndExecuteRequestAndThrowException() {
        // given
        String givenConsentId = "consentId";
        HttpRequest givenHttpRequest = dummyHttpRequest("startConsentAuthUrl");
        when(requestFactoryMock.generateConsentAuthorizationRequest(givenConsentId))
                .thenReturn(givenHttpRequest);

        HttpResponse givenSuccessfulHttpResponse =
                dummyHttpResponse(
                        BAD_REQUEST_STATUS, ConsentAuthorization.class, new ConsentAuthorization());
        when(clientMock.request(HttpResponse.class, givenHttpRequest))
                .thenReturn(givenSuccessfulHttpResponse);

        // when
        Throwable throwable =
                catchThrowable(() -> tested.startConsentAuthorization(givenConsentId));

        // then
        verify(requestFactoryMock).generateConsentAuthorizationRequest(givenConsentId);
        verify(clientMock).request(HttpResponse.class, givenHttpRequest);

        // and
        assertThat(throwable)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CHALLENGE_RESPONSE");
    }

    @Test
    public void selectConsentAuthorizationMethodShouldCallRequestFactoryAndExecuteRequest()
            throws LoginException {
        // given
        String givenConsentId = "consentId";
        String givenAuthorizationId = "authId";
        String givenMethodId = "methodId";
        HttpRequest givenHttpRequest = dummyHttpRequest("startConsentAuthUrl");
        when(requestFactoryMock.generateConsentAuthorizationMethodRequest(
                        givenConsentId, givenAuthorizationId, givenMethodId))
                .thenReturn(givenHttpRequest);

        HttpResponse givenSuccessfulHttpResponse =
                dummyHttpResponse(
                        OK_REQUEST_STATUS, ConsentAuthorization.class, new ConsentAuthorization());
        when(clientMock.request(HttpResponse.class, givenHttpRequest))
                .thenReturn(givenSuccessfulHttpResponse);

        // when
        tested.selectConsentAuthorizationMethod(
                givenConsentId, givenAuthorizationId, givenMethodId);

        // then
        verify(requestFactoryMock)
                .generateConsentAuthorizationMethodRequest(
                        givenConsentId, givenAuthorizationId, givenMethodId);
        verify(clientMock).request(HttpResponse.class, givenHttpRequest);
    }

    @Test
    public void
            selectConsentAuthorizationMethodShouldCallRequestFactoryAndExecuteRequestAndThrowException() {
        // given
        String givenConsentId = "consentId";
        String givenAuthorizationId = "authId";
        String givenMethodId = "methodId";
        HttpRequest givenHttpRequest = dummyHttpRequest("startConsentAuthUrl");
        when(requestFactoryMock.generateConsentAuthorizationMethodRequest(
                        givenConsentId, givenAuthorizationId, givenMethodId))
                .thenReturn(givenHttpRequest);

        HttpResponse givenSuccessfulHttpResponse =
                dummyHttpResponse(
                        BAD_REQUEST_STATUS, ConsentAuthorization.class, new ConsentAuthorization());
        when(clientMock.request(HttpResponse.class, givenHttpRequest))
                .thenReturn(givenSuccessfulHttpResponse);

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                tested.selectConsentAuthorizationMethod(
                                        givenConsentId, givenAuthorizationId, givenMethodId));

        // then
        verify(requestFactoryMock)
                .generateConsentAuthorizationMethodRequest(
                        givenConsentId, givenAuthorizationId, givenMethodId);
        verify(clientMock).request(HttpResponse.class, givenHttpRequest);

        // and
        assertThat(throwable)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CHALLENGE_RESPONSE");
    }

    @Test
    public void consentAuthorization2ndFactorShouldCallRequestFactoryAndExecuteRequest()
            throws LoginException {
        // given
        String givenConsentId = "consentId";
        String givenAuthorizationId = "authId";
        String givenTanCode = "tanCode";
        HttpRequest givenHttpRequest = dummyHttpRequest("startConsentAuthUrl");
        when(requestFactoryMock.generateConsentAuthorizationOtpRequest(
                        givenConsentId, givenAuthorizationId, givenTanCode))
                .thenReturn(givenHttpRequest);

        HttpResponse givenSuccessfulHttpResponse =
                dummyHttpResponse(
                        OK_REQUEST_STATUS, ConsentAuthorization.class, new ConsentAuthorization());
        when(clientMock.request(HttpResponse.class, givenHttpRequest))
                .thenReturn(givenSuccessfulHttpResponse);

        // when
        tested.consentAuthorization2ndFactor(givenConsentId, givenAuthorizationId, givenTanCode);

        // then
        verify(requestFactoryMock)
                .generateConsentAuthorizationOtpRequest(
                        givenConsentId, givenAuthorizationId, givenTanCode);
        verify(clientMock).request(HttpResponse.class, givenHttpRequest);
    }

    @Test
    public void
            consentAuthorization2ndFactorShouldCallRequestFactoryAndExecuteRequestAndThrowException() {
        // given
        String givenConsentId = "consentId";
        String givenAuthorizationId = "authId";
        String givenTanCode = "tanCode";
        HttpRequest givenHttpRequest = dummyHttpRequest("startConsentAuthUrl");
        when(requestFactoryMock.generateConsentAuthorizationOtpRequest(
                        givenConsentId, givenAuthorizationId, givenTanCode))
                .thenReturn(givenHttpRequest);

        HttpResponse givenSuccessfulHttpResponse =
                dummyHttpResponse(
                        BAD_REQUEST_STATUS, ConsentAuthorization.class, new ConsentAuthorization());
        when(clientMock.request(HttpResponse.class, givenHttpRequest))
                .thenReturn(givenSuccessfulHttpResponse);

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                tested.consentAuthorization2ndFactor(
                                        givenConsentId, givenAuthorizationId, givenTanCode));

        // then
        verify(requestFactoryMock)
                .generateConsentAuthorizationOtpRequest(
                        givenConsentId, givenAuthorizationId, givenTanCode);
        verify(clientMock).request(HttpResponse.class, givenHttpRequest);

        // and
        assertThat(throwable)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CHALLENGE_RESPONSE");
    }
}
