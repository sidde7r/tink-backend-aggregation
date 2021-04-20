package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.request.OtpAuthenticationRequestDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.request.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.AccountsSummaryResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.ErrorResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.LoginDetailsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.OpenAmErrorResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.OtpAuthenticationResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.TokenResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.TransactionsAndLockedEventsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response.AccountsSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response.LoginDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response.OtpAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response.TransactionsAndLockedEventsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.configuration.AktiaConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2TokenStorage;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
@Slf4j
public class AktiaApiClient {

    private static final String AUTH_TOKEN_PATH = "/cauth/oauth2/mobileauth/access_token";
    private static final String LOGIN_DETAILS_PATH = "/api/login/details";
    private static final String OTP_AUTH_PATH = "/api/login/otp/authenticate";
    private static final String ACCOUNT_SUMMARY_PATH = "/api/summary";
    private static final String ACCOUNT_RESOURCE_ID_KEY = "account-id";
    private static final String TRANSACTIONS_AND_LOCKED_EVENTS_PATH =
            String.format("/api/account/{%s}/transactionsAndLockedEvents", ACCOUNT_RESOURCE_ID_KEY);

    private final TinkHttpClient httpClient;
    private final AktiaConfiguration aktiaConfiguration;
    private final OAuth2TokenStorage tokenStorage;

    public TokenResponseDto retrieveAccessToken(String username, String password) {
        final TokenRequest tokenRequest = new TokenRequest(username, password);

        try {
            return httpClient
                    .request(createAuthUrl())
                    .body(tokenRequest, MediaType.APPLICATION_FORM_URLENCODED)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Basic " + aktiaConfiguration.getBasicAuthHeaderValue())
                    .post(TokenResponseDto.class);
        } catch (HttpResponseException exception) {
            HttpResponse response = exception.getResponse();
            String exceptionMessage = response.getBody(String.class);
            if (exceptionMessage.contains("invalid_grant")
                    && (response.getStatus() == HttpStatus.SC_BAD_REQUEST)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
            throw exception;
        }
    }

    public LoginDetailsResponse getLoginDetails() {
        try {
            final LoginDetailsResponseDto loginDetailsResponseDto =
                    httpClient
                            .request(createApiUrl(LOGIN_DETAILS_PATH))
                            .addBearerToken(getOAuth2Token())
                            .get(LoginDetailsResponseDto.class);

            return new LoginDetailsResponse(loginDetailsResponseDto);
        } catch (HttpResponseException ex) {
            final OpenAmErrorResponseDto errorResponseDto =
                    handleHttpResponseException(ex, OpenAmErrorResponseDto.class);

            log.error(
                    "GET login details request returned error: '{}' with description: '{}'.",
                    errorResponseDto.getError(),
                    errorResponseDto.getErrorDescription());
            return new LoginDetailsResponse(errorResponseDto);
        }
    }

    public OtpAuthenticationResponse authenticateWithOtp(String otpCode) {
        final OtpAuthenticationRequestDto otpAuthenticationRequest =
                new OtpAuthenticationRequestDto(otpCode);

        try {
            final OtpAuthenticationResponseDto otpAuthenticationResponseDto =
                    httpClient
                            .request(createApiUrl(OTP_AUTH_PATH))
                            .body(otpAuthenticationRequest, MediaType.APPLICATION_JSON)
                            .addBearerToken(getOAuth2Token())
                            .post(OtpAuthenticationResponseDto.class);

            return new OtpAuthenticationResponse(otpAuthenticationResponseDto);
        } catch (HttpResponseException ex) {
            final ErrorResponseDto errorResponseDto =
                    handleHttpResponseException(ex, ErrorResponseDto.class);

            log.error(
                    "POST otp authentication request returned error with code: '{}' and message: '{}'.",
                    errorResponseDto.getErrorCode(),
                    errorResponseDto.getMessage());
            return new OtpAuthenticationResponse(errorResponseDto);
        }
    }

    public AccountsSummaryResponse getAccountsSummary() {
        try {
            final AccountsSummaryResponseDto accountsSummaryResponseDto =
                    httpClient
                            .request(createApiUrl(ACCOUNT_SUMMARY_PATH))
                            .addBearerToken(getOAuth2Token())
                            .get(AccountsSummaryResponseDto.class);

            return new AccountsSummaryResponse(accountsSummaryResponseDto);
        } catch (HttpResponseException ex) {
            final OpenAmErrorResponseDto errorResponseDto =
                    handleHttpResponseException(ex, OpenAmErrorResponseDto.class);

            log.error(
                    "GET accounts summary request returned error: '{}' with description: '{}'.",
                    errorResponseDto.getError(),
                    errorResponseDto.getErrorDescription());
            return new AccountsSummaryResponse(errorResponseDto);
        }
    }

    public TransactionsAndLockedEventsResponse getTransactionsAndLockedEvents(
            String accountId, String continuationKey) {
        try {
            final URL url =
                    createApiUrl(TRANSACTIONS_AND_LOCKED_EVENTS_PATH)
                            .parameter(ACCOUNT_RESOURCE_ID_KEY, accountId);

            RequestBuilder requestBuilder =
                    httpClient.request(url).addBearerToken(getOAuth2Token());

            if (StringUtils.isNotBlank(continuationKey)) {
                requestBuilder = requestBuilder.queryParam("continuationKey", continuationKey);
            }

            final TransactionsAndLockedEventsResponseDto transactionsAndLockedEventsResponseDto =
                    requestBuilder.get(TransactionsAndLockedEventsResponseDto.class);

            return new TransactionsAndLockedEventsResponse(transactionsAndLockedEventsResponseDto);
        } catch (HttpResponseException ex) {
            final OpenAmErrorResponseDto errorResponseDto =
                    handleHttpResponseException(ex, OpenAmErrorResponseDto.class);

            log.error(
                    "GET transactions and locked events request returned error: '{}' with description: '{}'.",
                    errorResponseDto.getError(),
                    errorResponseDto.getErrorDescription());
            return new TransactionsAndLockedEventsResponse(errorResponseDto);
        }
    }

    private URL createAuthUrl() {
        return new URL("https://mobile-auth2.aktia.fi" + AUTH_TOKEN_PATH);
    }

    private URL createApiUrl(String path) {
        return new URL("https://mobile-gateway2.aktia.fi" + path);
    }

    private OAuth2Token getOAuth2Token() {
        return tokenStorage
                .getToken()
                .orElseThrow(
                        () -> new IllegalArgumentException("Access token not found in storage."));
    }

    private static <T> T handleHttpResponseException(
            HttpResponseException httpResponseException, Class<T> clazz) {
        if (httpResponseException.getResponse().hasBody()) {
            try {
                return httpResponseException.getResponse().getBody(clazz);

            } catch (HttpClientException | HttpResponseException d) {
                throw httpResponseException;
            }
        }

        throw httpResponseException;
    }
}
