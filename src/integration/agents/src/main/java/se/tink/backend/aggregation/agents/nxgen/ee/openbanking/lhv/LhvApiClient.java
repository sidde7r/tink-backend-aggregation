package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.GrantType;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.QueryKey;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.Url;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.rpc.AuthorisationResponse;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.rpc.AuthorisationStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.configuration.LhvConfiguration;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.entities.AccountSummaryEntity;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc.AccountSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc.ConsentAccess;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc.ConsentAccessAccounts;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc.TransactionsResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.api.Psd2Headers.Keys;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

@Slf4j
public class LhvApiClient {
    private final TinkHttpClient client;
    protected LhvConfiguration configuration;
    private final CredentialsRequest credentialsRequest;
    private final String redirectUrl;
    private final PersistentStorage persistentStorage;
    private final LocalDate todaysDate;

    public LhvApiClient(
            TinkHttpClient client,
            AgentConfiguration<LhvConfiguration> agentConfiguration,
            CredentialsRequest credentialsRequest,
            PersistentStorage persistentStorage,
            LocalDate todaysDate) {
        this.client = client;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.credentialsRequest = credentialsRequest;
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.persistentStorage = persistentStorage;
        this.todaysDate = todaysDate;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(Keys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(Keys.CLIENT_ID, configuration.getClientId())
                .queryParam(Keys.CLIENT_ID, configuration.getClientId())
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    public AuthorisationStatusResponse checkAuthorisationStatus(String authorisationId) {
        return client.request(Url.AUTH_STATUS.parameter(IdTags.AUTHORIZATION_ID, authorisationId))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(Keys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(AuthorisationStatusResponse.class);
    }

    public TokenResponse retrieveAccessToken(String code) {

        return getAccessToken(
                TokenRequest.builder()
                        .grantType(GrantType.AUTHORIZATION_CODE)
                        .clientId(configuration.getClientId())
                        .code(code)
                        .build());
    }

    public TokenResponse getAccessToken(TokenRequest tokenRequest) {
        String body =
                Form.builder()
                        .put("client_id", configuration.getClientId())
                        .put("grant_type", GrantType.AUTHORIZATION_CODE)
                        .put("code", tokenRequest.getCode())
                        .build()
                        .serialize();
        return client.request(Url.GET_OAUTH2_TOKEN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(TokenResponse.class, body);
    }

    public TokenResponse getRefreshToken(String refreshToken) {
        return getAccessToken(
                TokenRequest.builder()
                        .grantType(GrantType.REFRESH_TOKEN)
                        .clientId(configuration.getClientId())
                        .refreshToken(refreshToken)
                        .build());
    }

    public AuthorisationResponse login(
            LoginRequest loginRequest, String psuId, String psuCorporateId) throws LoginException {

        return createRequest(Url.AUTH)
                .header(Keys.PSU_ID, psuId)
                .header(QueryKey.PSU_CORPORATE_ID, psuCorporateId)
                .post(AuthorisationResponse.class, loginRequest);
    }

    public AccountSummaryResponse getAccountSummary() {

        return client.request(Url.ACCOUNT_SUMMARY_LIST)
                .addBearerToken(getTokenFromStorage())
                .header(Keys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .get(AccountSummaryResponse.class);
    }

    public AccountResponse getAccounts() {

        return client.request(Url.ACCOUNT_LIST)
                .addBearerToken(getTokenFromStorage())
                .header(Keys.CONSENT_ID, persistentStorage.get(StorageKeys.USER_CONSENT_ID))
                .header(Keys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .get(AccountResponse.class);
    }

    public ConsentResponse getConsent(AccountSummaryResponse accountSummaryResponse) {
        final OAuth2Token token = getTokenFromStorage();
        final ConsentRequest request = generateConsentRequest(accountSummaryResponse);
        final String ip = credentialsRequest.getUserAvailability().getOriginatingUserIp();
        return client.request(Url.CONSENT)
                .addBearerToken(token)
                .header(Keys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(Keys.PSU_IP_ADDRESS, ip)
                .body(request, MediaType.APPLICATION_JSON)
                .header(QueryKey.TPP_REDIRECT_URI, redirectUrl)
                .post(ConsentResponse.class);
    }

    private ConsentRequest generateConsentRequest(AccountSummaryResponse accountSummaryResponse) {
        List<ConsentAccessAccounts> consentedAccounts = new ArrayList<>();

        for (AccountSummaryEntity accountSummaryEntity :
                accountSummaryResponse.getAccountSummaryList()) {
            consentedAccounts.add(new ConsentAccessAccounts(accountSummaryEntity.getIban()));
        }

        final ConsentAccess consentAccess =
                new ConsentAccess(QueryValues.ALL_ACCOUNTS, consentedAccounts);

        return new ConsentRequest(
                consentAccess,
                QueryValues.RECURRING_INDICATOR,
                todaysDate.plusDays(90).toString(),
                QueryValues.FREQUENCY_PER_DAY,
                QueryValues.COMBINED_SERVICE_INDICATOR);
    }

    public boolean isConsentValid() {
        String consentId = persistentStorage.get(StorageKeys.USER_CONSENT_ID);
        if (Strings.isNullOrEmpty(consentId)) {
            return false;
        }

        return getConsentStatus(consentId).getConsentStatus().equalsIgnoreCase(ConsentStatus.VALID);
    }

    public ConsentResponse getConsentStatus(String consentId) {
        return client.request(Url.CONSENT_STATUS.parameter(IdTags.CONSENT_ID, consentId))
                .addBearerToken(getTokenFromStorage())
                .header(Keys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .get(ConsentResponse.class);
    }

    public TransactionsResponse fetchTransactions(String resourceId, String dateFrom) {
        return client.request(Url.TRANSACTIONS.parameter(IdTags.RESOURCE_ID, resourceId))
                .header(Keys.CONSENT_ID, persistentStorage.get(StorageKeys.USER_CONSENT_ID))
                .queryParam(IdTags.DATE_FROM, dateFrom)
                .queryParam(IdTags.BOOKING_STATUS, QueryValues.BOOKING_STATUS)
                .header(Keys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .addBearerToken(getTokenFromStorage())
                .get(TransactionsResponse.class);
    }

    protected OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> {
                            log.info(ErrorMessages.TOKEN_NOT_FOUND);
                            return new IllegalStateException(
                                    SessionError.SESSION_EXPIRED.exception());
                        });
    }

    public BalanceResponse fetchAccountBalance(String resourceId) {
        return client.request(Url.BALANCE.parameter(IdTags.RESOURCE_ID, resourceId))
                .addBearerToken(getTokenFromStorage())
                .header(Keys.CONSENT_ID, persistentStorage.get(StorageKeys.USER_CONSENT_ID))
                .header(Keys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .get(BalanceResponse.class);
    }
}
