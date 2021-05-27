package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag;

import java.time.LocalDate;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.AspspId;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.PathVariables;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.entities.PsuDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.rpc.AuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.rpc.FinalizeAuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.rpc.FinalizeAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.rpc.InitAuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.rpc.OauthEndpointsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.rpc.SelectAuthenticationMethodRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.fetcher.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.fetcher.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.agents.utils.charsetguesser.CharsetGuesser;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Slf4j
@RequiredArgsConstructor
public class BankverlagApiClient {

    private final TinkHttpClient client;
    private final BankverlagHeaderValues headerValues;
    private final BankverlagStorage storage;
    private final RandomValueGenerator randomValueGenerator;
    private final LocalDateTimeSource localDateTimeSource;

    private RequestBuilder createRequest(URL url) {
        if (url.get().contains("{" + PathVariables.ASPSP_ID + "}")) {
            url = url.parameter(PathVariables.ASPSP_ID, headerValues.getAspspId());
        }

        return client.request(url)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.API_KEY, HeaderValues.API_KEY_VALUE)
                .header(HeaderKeys.X_REQUEST_ID, randomValueGenerator.getUUID().toString())
                .header(HeaderKeys.PSU_IP_ADDRESS, headerValues.getUserIp());
    }

    private RequestBuilder createRequestInSession(URL url, String consentId) {
        String auth = storage.getToken().map(OAuth2Token::toAuthorizeHeader).orElse(null);
        return createRequest(url)
                .header(HeaderKeys.CONSENT_ID, consentId)
                .header("Authorization", auth);
    }

    public OauthEndpointsResponse getOauthEndpoints(String authorizationEndpointSource) {
        return client.request(authorizationEndpointSource)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(OauthEndpointsResponse.class);
    }

    public ConsentResponse createConsent() {
        LocalDate validUntil = localDateTimeSource.now().toLocalDate().plusDays(90);
        ConsentRequest consentRequest =
                new ConsentRequest(
                        new AccessEntity(),
                        true,
                        validUntil.toString(),
                        FormValues.FREQUENCY_PER_DAY,
                        false);

        return createRequest(Urls.CONSENT)
                .header(HeaderKeys.TPP_REDIRECT_PREFERRED, headerValues.isRedirect())
                .header("TPP-Redirect-URI", headerValues.getRedirectUrl())
                .header("TPP-Nok-Redirect-URI", headerValues.getRedirectUrl())
                .post(ConsentResponse.class, consentRequest);
    }

    public AuthorizationResponse initializeAuthorization(
            String url, String username, String password) {
        try {
            AuthorizationResponse authorizationResponse =
                    createRequest(new URL(url))
                            .header(HeaderKeys.PSU_ID, username)
                            .post(
                                    AuthorizationResponse.class,
                                    new InitAuthorizationRequest(new PsuDataEntity(password)));
            // NZG-283 temporary login
            log.info(
                    "SUCCESS_LOGIN username charset: [{}]  password charset: [{}]",
                    CharsetGuesser.getCharset(username),
                    CharsetGuesser.getCharset(password));
            return authorizationResponse;
        } catch (HttpResponseException e) {
            // NZG-283 temporary login
            log.info(
                    "FAILED_LOGIN username charset: [{}]  password charset: [{}]",
                    CharsetGuesser.getCharset(username),
                    CharsetGuesser.getCharset(password));
            BankverlagErrorHandler.handleError(
                    e, BankverlagErrorHandler.ErrorSource.AUTHORISATION_USERNAME_PASSWORD);
            throw e;
        }
    }

    public AuthorizationResponse selectAuthorizationMethod(String url, String methodId) {
        try {
            return createRequest(new URL(url))
                    .put(
                            AuthorizationResponse.class,
                            new SelectAuthenticationMethodRequest(methodId));
        } catch (HttpResponseException e) {
            BankverlagErrorHandler.handleError(
                    e, BankverlagErrorHandler.ErrorSource.AUTHORISATION_SELECT_METHOD);
            throw e;
        }
    }

    public AuthorizationResponse getAuthorizationStatus(String url) {
        return createRequest(new URL(url)).get(AuthorizationResponse.class);
    }

    public FinalizeAuthorizationResponse finalizeAuthorization(String url, String otp) {
        try {
            return createRequest(new URL(url))
                    .put(
                            FinalizeAuthorizationResponse.class,
                            new FinalizeAuthorizationRequest(otp));
        } catch (HttpResponseException e) {
            BankverlagErrorHandler.handleError(
                    e, BankverlagErrorHandler.ErrorSource.AUTHORISATION_OTP);
            throw e;
        }
    }

    public ConsentStatusResponse getConsentStatus(String consentId) {
        return createRequest(Urls.CONSENT_STATUS.parameter(PathVariables.CONSENT_ID, consentId))
                .get(ConsentStatusResponse.class);
    }

    public ConsentDetailsResponse getConsentDetails(String consentId) {
        return createRequest(Urls.CONSENT_DETAILS.parameter(PathVariables.CONSENT_ID, consentId))
                .get(ConsentDetailsResponse.class);
    }

    public FetchAccountsResponse fetchAccounts(String consentId) {
        return createRequestInSession(Urls.FETCH_ACCOUNTS, consentId)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .get(FetchAccountsResponse.class);
    }

    public FetchBalancesResponse getAccountBalance(String consentId, String accountId) {
        return createRequestInSession(
                        Urls.FETCH_BALANCES.parameter(PathVariables.ACCOUNT_ID, accountId),
                        consentId)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .get(FetchBalancesResponse.class);
    }

    public String fetchTransactions(String consentId, String accountId, LocalDate startDate) {
        if (AspspId.ASPSP_WITH_URI_FOR_TRANSACTIONS.contains(headerValues.getAspspId())) {
            return getTransactionsFromResponseUrl(consentId, accountId, startDate);
        } else {
            return getTransactionsFromResponse(consentId, accountId, startDate);
        }
    }

    private String getTransactionsFromResponse(
            String consentId, String accountId, LocalDate startDate) {
        return createRequestInSession(
                        Urls.FETCH_TRANSACTIONS
                                .parameter(PathVariables.ACCOUNT_ID, accountId)
                                .queryParam(QueryKeys.DATE_FROM, startDate.toString())
                                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKED),
                        consentId)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
    }

    private String getTransactionsFromResponseUrl(
            String consentId, String accountId, LocalDate startDate) {

        FetchTransactionsResponse fetchTransactionsResponse =
                createRequestInSession(
                                Urls.FETCH_TRANSACTIONS
                                        .parameter(PathVariables.ACCOUNT_ID, accountId)
                                        .queryParam(QueryKeys.DATE_FROM, startDate.toString())
                                        .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKED),
                                consentId)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .get(FetchTransactionsResponse.class);

        return createRequestInSession(
                        fetchTransactionsResponse.getLinks().getDownload().getHref(), consentId)
                .type(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .get(String.class);
    }

    public TokenResponse sendToken(String tokenEndpoint, String tokenEntity) {
        return createRequest(new URL(tokenEndpoint))
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, tokenEntity);
    }
}
