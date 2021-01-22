package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static se.tink.backend.aggregation.agents.exceptions.errors.LoginError.INCORRECT_CHALLENGE_RESPONSE;
import static se.tink.backend.aggregation.agents.exceptions.errors.LoginError.INCORRECT_CREDENTIALS;
import static se.tink.backend.aggregation.agents.exceptions.errors.LoginError.NOT_SUPPORTED;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbStorage;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;

public class DkbAuthApiClient {

    private final TinkHttpClient httpClient;
    private final DkbAuthRequestsFactory requestsFactory;
    private final DkbStorage storage;

    public DkbAuthApiClient(
            TinkHttpClient httpClient, DkbAuthRequestsFactory requestsFactory, DkbStorage storage) {
        this.httpClient = httpClient;
        this.requestsFactory = requestsFactory;
        this.storage = storage;
    }

    Wso2Token getWso2Token() throws LoginException {
        HttpRequest request = requestsFactory.generateWso2TokenRequest();
        return executeHttpRequest(request, Wso2Token.class, NOT_SUPPORTED);
    }

    AuthResult authenticate1stFactor(String username, String password) throws LoginException {
        HttpRequest request = requestsFactory.generateAuth1stFactorRequest(username, password);
        return executeHttpRequest(request, AuthResult.class, INCORRECT_CREDENTIALS);
    }

    AuthResult select2ndFactorAuthMethod(String methodId) throws LoginException {
        HttpRequest request = requestsFactory.generateAuthMethodSelectionRequest(methodId);
        return executeHttpRequest(request, AuthResult.class, INCORRECT_CREDENTIALS);
    }

    AuthResult submit2ndFactorTanCode(String code) throws LoginException {
        HttpRequest request = requestsFactory.generateTanSubmissionRequest(code);
        return executeHttpRequest(request, AuthResult.class, INCORRECT_CREDENTIALS);
    }

    ConsentResponse createConsent(LocalDate validUntil) {
        HttpRequest request = requestsFactory.generateCreateConsentRequest(validUntil);
        return executeHttpRequest(request, ConsentResponse.class);
    }

    ConsentDetailsResponse getConsentDetails(String consentId) {
        HttpRequest request = requestsFactory.generateGetConsentRequest(consentId);
        return executeHttpRequest(request, ConsentDetailsResponse.class);
    }

    ConsentAuthorization startConsentAuthorization(String consentId) throws LoginException {
        HttpRequest request = requestsFactory.generateConsentAuthorizationRequest(consentId);
        return executeHttpRequest(
                request, ConsentAuthorization.class, INCORRECT_CHALLENGE_RESPONSE);
    }

    ConsentAuthorization selectConsentAuthorizationMethod(
            String consentId, String authorizationId, String methodId) throws LoginException {
        HttpRequest request =
                requestsFactory.generateConsentAuthorizationMethodRequest(
                        consentId, authorizationId, methodId);
        return executeHttpRequest(
                request, ConsentAuthorization.class, INCORRECT_CHALLENGE_RESPONSE);
    }

    ConsentAuthorization consentAuthorization2ndFactor(
            String consentId, String authorizationId, String code) throws LoginException {
        HttpRequest request =
                requestsFactory.generateConsentAuthorizationOtpRequest(
                        consentId, authorizationId, code);
        return executeHttpRequest(
                request, ConsentAuthorization.class, INCORRECT_CHALLENGE_RESPONSE);
    }

    private <T> T executeHttpRequest(
            HttpRequest httpRequest, Class<T> returnType, LoginError errorType)
            throws LoginException {
        return Optional.of(httpRequest)
                .map(this::executeHttpRequest)
                .map(response -> processHttpResponse(response, returnType))
                .filter(ExternalApiCallResult::isSuccess)
                .map(ExternalApiCallResult::getResult)
                .orElseThrow(errorType::exception);
    }

    private <T> T executeHttpRequest(HttpRequest httpRequest, Class<T> returnType) {
        return Optional.of(httpRequest)
                .map(this::executeHttpRequest)
                .map(response -> processHttpResponse(response, returnType))
                .filter(ExternalApiCallResult::isSuccess)
                .map(ExternalApiCallResult::getResult)
                .orElse(null);
    }

    private HttpResponse executeHttpRequest(HttpRequest httpRequest) {
        return httpClient.request(HttpResponse.class, httpRequest);
    }

    private <T> ExternalApiCallResult<T> processHttpResponse(
            HttpResponse response, Class<T> returnType) {
        storeSessionValues(response);
        return ExternalApiCallResult.of(response.getBody(returnType), response.getStatus());
    }

    private void storeSessionValues(HttpResponse response) {
        extractCookieValue(response.getCookies(), "JSESSIONID").ifPresent(storage::setJsessionid);
        extractHeaderValue(response.getHeaders(), "X-XSRF-TOKEN").ifPresent(storage::setXsrfToken);
    }

    private Optional<String> extractCookieValue(List<NewCookie> cookies, String cookieName) {
        return cookies.stream()
                .filter(cookie -> cookie.getName().compareToIgnoreCase(cookieName) == 0)
                .map(Cookie::getValue)
                .findFirst();
    }

    private Optional<String> extractHeaderValue(
            MultivaluedMap<String, String> headers, String headerName) {
        return Optional.ofNullable(headers).map(h -> h.getFirst(headerName));
    }
}
