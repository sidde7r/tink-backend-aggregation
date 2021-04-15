package se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum;

import java.time.LocalDate;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.authenticator.data.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.authenticator.data.UnauthorizedResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher.data.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher.data.TransactionsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class MediolanumApiClient {

    private static final String BASE_URL = "https://api.mediolanum.it/psd2bg/03062";
    private static final String TOKEN_URL = "https://api.mediolanum.it:9090/oauth/token";

    private static final String CONSENT = "/v1/consents";
    private static final String CONSENT_DETAILS = "/v1/consents/{consentId}";
    private static final String GET_ACCOUNTS = "/v1/accounts";
    private static final String GET_TRANSACTIONS = "/v1/accounts/{accountId}/transactions";

    private static final String ACCOUNT_ID = "accountId";
    private static final String CONSENT_ID = "consentId";

    private final TinkHttpClient client;
    private final RandomValueGenerator randomValueGenerator;
    private final LocalDateTimeSource localDateTimeSource;
    private final MediolanumConfiguration configuration;
    private final MediolanumStorage storage;

    // Need eidas configuration to readd using proxy after removing it for just one request
    @Setter private EidasProxyConfiguration eidasProxy;

    protected RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON)
                .header(Psd2Headers.Keys.X_REQUEST_ID, randomValueGenerator.getUUID())
                .header(Psd2Headers.Keys.PSU_IP_ADDRESS, configuration.getUserIp());
    }

    public UnauthorizedResponse getRedirectUrl() {
        UnauthorizedResponse response;
        try {
            response =
                    createRequest(new URL(BASE_URL + CONSENT))
                            .header(Psd2Headers.Keys.TPP_REDIRECT_URI, "dummy")
                            .body(buildConsentRequest())
                            .post(UnauthorizedResponse.class);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 401 && e.getResponse().hasBody()) {
                response = e.getResponse().getBody(UnauthorizedResponse.class);
            } else {
                throw e;
            }
        }
        return response;
    }

    public ConsentResponse createConsent(String state) {
        URL redirectWithState = new URL(configuration.getRedirectUrl()).queryParam("state", state);
        return createRequest(new URL(BASE_URL + CONSENT))
                .header(Psd2Headers.Keys.TPP_REDIRECT_URI, redirectWithState)
                .header(
                        Psd2Headers.Keys.TPP_NOK_REDIRECT_URI,
                        redirectWithState.queryParam("nok", "true"))
                .addBearerToken(getToken())
                .body(buildConsentRequest())
                .post(ConsentResponse.class);
    }

    private ConsentRequest buildConsentRequest() {
        AccessEntity accessEntity = new AccessEntity();
        accessEntity.setAvailableAccounts(AccessEntity.ALL_ACCOUNTS);
        return new ConsentRequest(
                accessEntity,
                true,
                localDateTimeSource.now().toLocalDate().plusDays(89).toString(),
                4,
                false);
    }

    public TokenResponse sendToken(String tokenEntity) {
        client.resetInternalClient();
        client.clearEidasProxy();
        TokenResponse tokenResponse =
                client.request(new URL(TOKEN_URL))
                        .type(MediaType.APPLICATION_FORM_URLENCODED)
                        .post(TokenResponse.class, tokenEntity);
        client.resetInternalClient();
        client.setEidasProxy(eidasProxy);
        return tokenResponse;
    }

    public ConsentDetailsResponse fetchConsentDetails(String consentId) {
        return createRequest(new URL(BASE_URL + CONSENT_DETAILS).parameter(CONSENT_ID, consentId))
                .addBearerToken(getToken())
                .get(ConsentDetailsResponse.class);
    }

    public AccountsResponse fetchAccounts() {
        return createRequest(new URL(BASE_URL + GET_ACCOUNTS).queryParam("withBalance", "true"))
                .header(Psd2Headers.Keys.CONSENT_ID, storage.getConsentId())
                .addBearerToken(getToken())
                .get(AccountsResponse.class);
    }

    public TransactionsResponse fetchTransactions(String accountId, LocalDate from, LocalDate to) {
        return createRequest(
                        new URL(BASE_URL + GET_TRANSACTIONS)
                                .parameter(ACCOUNT_ID, accountId)
                                .queryParam("bookingStatus", "booked")
                                .queryParam("dateFrom", from.toString())
                                .queryParam("dateTo", to.toString()))
                .header(Psd2Headers.Keys.CONSENT_ID, storage.getConsentId())
                .addBearerToken(getToken())
                .get(TransactionsResponse.class);
    }

    private OAuth2Token getToken() {
        return storage.getToken()
                .orElseThrow(
                        () ->
                                SessionError.SESSION_EXPIRED.exception(
                                        "Access token not found in storage when expected!"));
    }
}
