package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag;

import static se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity.ALL_ACCOUNTS;

import java.time.LocalDate;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.PathVariables;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.Urls;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.FinalizeAuthorizationRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.OauthEndpointsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.PsuDataEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.SelectAuthorizationMethodRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.TokenResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
@Slf4j
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
                        new AccessEntity(ALL_ACCOUNTS),
                        true,
                        validUntil.toString(),
                        FormValues.FREQUENCY_PER_DAY,
                        false);

        return createRequest(Urls.CONSENT).post(ConsentResponse.class, consentRequest);
    }

    public AuthorizationResponse initializeAuthorization(
            String url, String username, String password) {

        return createRequest(new URL(url))
                .header(HeaderKeys.PSU_ID, username)
                .post(
                        AuthorizationResponse.class,
                        new AuthorizationRequest(new PsuDataEntity(password)));
    }

    public AuthorizationResponse selectAuthorizationMethod(String url, String methodId) {

        return createRequest(new URL(url))
                .put(AuthorizationResponse.class, new SelectAuthorizationMethodRequest(methodId));
    }

    public AuthorizationResponse getAuthorizationStatus(String url) {
        return createRequest(new URL(url)).get(AuthorizationResponse.class);
    }

    public AuthorizationResponse finalizeAuthorization(String url, String otp) {

        return createRequest(new URL(url))
                .put(AuthorizationResponse.class, new FinalizeAuthorizationRequest(otp));
    }

    public ConsentResponse getConsentStatus(String consentId) {
        return createRequest(Urls.CONSENT_STATUS.parameter(PathVariables.CONSENT_ID, consentId))
                .get(ConsentResponse.class);
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
        return createRequestInSession(
                        Urls.FETCH_TRANSACTIONS
                                .parameter(PathVariables.ACCOUNT_ID, accountId)
                                .queryParam(QueryKeys.DATE_FROM, startDate.toString())
                                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKED),
                        consentId)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
    }

    public HttpResponse getTransactionsZipFile(
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

        return getTransactionsFile(
                consentId, fetchTransactionsResponse.getLinks().getDownload().getHref());
    }

    private HttpResponse getTransactionsFile(String consentId, URL file) {
        return createRequestInSession(file, consentId)
                .type(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .get(HttpResponse.class);
    }

    public TokenResponse sendToken(String tokenEndpoint, String tokenEntity) {
        return createRequest(new URL(tokenEndpoint))
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, tokenEntity);
    }
}
