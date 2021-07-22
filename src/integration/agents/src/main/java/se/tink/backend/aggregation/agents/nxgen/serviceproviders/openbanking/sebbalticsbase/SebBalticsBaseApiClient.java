package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsCommonConstants.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsCommonConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsCommonConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsCommonConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsCommonConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsCommonConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.AuthMethodSelectionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.ConsentAuthMethod;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.ConsentAuthMethodSelectionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.ConsentAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.DecoupledAuthMethod;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.DecoupledAuthRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.DecoupledAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.DecoupledTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.configuration.SebBalticsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.date.DateFormat.Zone;

public class SebBalticsBaseApiClient {

    protected final TinkHttpClient client;
    protected final PersistentStorage persistentStorage;
    protected final CredentialsRequest credentialsRequest;
    protected SebBalticsConfiguration configuration;

    public SebBalticsBaseApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            CredentialsRequest credentialsRequest) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.credentialsRequest = credentialsRequest;
    }

    public void setConfiguration(SebBalticsConfiguration configuration) {
        this.configuration = configuration;
    }

    public DecoupledAuthResponse startDecoupledAuthorization(
            DecoupledAuthRequest authorizationRequest) {
        return createRequest(Urls.DECOUPLED_AUTHORIZATION)
                .post(DecoupledAuthResponse.class, authorizationRequest);
    }

    public DecoupledAuthResponse getDecoupledAuthStatus(String authRequestId) {
        return createRequest(
                        Urls.DECOUPLED_AUTHORIZATION
                                .concat(URL.URL_SEPARATOR)
                                .concat(authRequestId))
                .get(DecoupledAuthResponse.class);
    }

    public AuthMethodSelectionResponse updateDecoupledAuthStatus(
            DecoupledAuthMethod decoupledAuthMethod, String authRequestId) {
        return createRequest(
                        Urls.DECOUPLED_AUTHORIZATION
                                .concat(URL.URL_SEPARATOR)
                                .concat(authRequestId))
                .patch(AuthMethodSelectionResponse.class, decoupledAuthMethod);
    }

    public TokenResponse getDecoupledToken(DecoupledTokenRequest tokenRequest) {
        return createRequest(Urls.DECOUPLED_TOKEN).post(TokenResponse.class, tokenRequest);
    }

    public ConsentResponse createNewConsent(ConsentRequest consentRequest) {
        return createRequest(Urls.NEW_CONSENT)
                .header(
                        HeaderKeys.PSU_IP_ADDRESS,
                        credentialsRequest.getUserAvailability().getOriginatingUserIp())
                .addBearerToken(getTokenFromStorage())
                .post(ConsentResponse.class, consentRequest);
    }

    public ConsentAuthorizationResponse startConsentAuthorization(String consentId) {
        return createRequest(Urls.CONSENT_AUTHORIZATION.parameter(IdTags.CONSENT_ID, consentId))
                .addBearerToken(getTokenFromStorage())
                .post(ConsentAuthorizationResponse.class);
    }

    public ConsentAuthMethodSelectionResponse updateConsentAuthorization(
            ConsentAuthMethod consentAuthMethod, String authorizationId, String consentId) {

        return createRequest(
                        Urls.CONSENT_AUTHORIZATION
                                .parameter(IdTags.CONSENT_ID, consentId)
                                .concat(URL.URL_SEPARATOR)
                                .concat(authorizationId))
                .addBearerToken(getTokenFromStorage())
                .patch(ConsentAuthMethodSelectionResponse.class, consentAuthMethod);
    }

    public boolean isConsentValid() {
        String consentId = persistentStorage.get(StorageKeys.USER_CONSENT_ID);
        if (Strings.isNullOrEmpty(consentId)) {
            return false;
        }

        return getConsentStatus(consentId).getConsentStatus().equalsIgnoreCase(ConsentStatus.VALID);
    }

    public ConsentResponse getConsentStatus(String consentId) {
        return createRequest(Urls.CONSENT_STATUS.parameter(IdTags.CONSENT_ID, consentId))
                .addBearerToken(getTokenFromStorage())
                .get(ConsentResponse.class);
    }

    // fetch list of PSU's accounts without the consent
    public AccountsResponse fetchAccountsList() {
        return createRequest(Urls.ACCOUNTS_LIST)
                .addBearerToken(getTokenFromStorage())
                .get(AccountsResponse.class);
    }

    // fetch PSU's accounts under a given PSU consent
    public AccountsResponse fetchAccounts() {
        return createRequestInSession(Urls.ACCOUNTS).get(AccountsResponse.class);
    }

    public TransactionsResponse fetchTransactions(URL urlAddress) {
        RequestBuilder requestBuilder = createRequestInSession(urlAddress);
        return requestBuilder.get(TransactionsResponse.class);
    }

    public TransactionsResponse fetchTransactions(String accountId, LocalDate from, LocalDate to) {

        return createRequestInSession((Urls.TRANSACTIONS).parameter(IdTags.ACCOUNT_ID, accountId))
                .queryParam(QueryKeys.DATE_FROM, from.toString())
                .queryParam(QueryKeys.DATE_TO, to.toString())
                .get(TransactionsResponse.class);
    }

    public BalanceResponse fetchAccountBalances(String accountId) {

        return createRequestInSession((Urls.BALANCES).parameter(IdTags.ACCOUNT_ID, accountId))
                .get(BalanceResponse.class);
    }

    protected RequestBuilder createRequestInSession(URL url) {
        RequestBuilder requestBuilder =
                createRequest(url)
                        .header(
                                HeaderKeys.CONSENT_ID,
                                persistentStorage.get(StorageKeys.USER_CONSENT_ID))
                        .addBearerToken(getTokenFromStorage());

        requestBuilder.header(
                HeaderKeys.PSU_INVOLVED, credentialsRequest.getUserAvailability().isUserPresent());

        final Credentials credentials = credentialsRequest.getCredentials();

        requestBuilder
                .header(HeaderKeys.PSU_ID, credentials.getField(Key.USERNAME))
                .header(HeaderKeys.PSU_CORPORATE_ID, credentials.getField(Key.CORPORATE_ID));

        return requestBuilder;
    }

    protected OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Can not find token!"));
    }

    public String getLocalDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z");
        return ZonedDateTime.now(ZoneId.of(Zone.GMT)).format(formatter);
    }

    protected RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(HeaderKeys.CLIENT_ID, configuration.getClientId())
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.DATE, getLocalDateTime());
    }
}
