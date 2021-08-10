package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics;

import com.google.common.base.Strings;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsConstants.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.AuthMethodSelectionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.ConsentAuthMethod;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.ConsentAuthMethodSelectionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.ConsentAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.DecoupledAuthMethod;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.DecoupledAuthRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.DecoupledAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.DecoupledTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.configuration.SebBalticsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.api.Psd2Headers.Keys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.date.DateFormat.Zone;

public class SebBalticsApiClient {

    protected final TinkHttpClient client;
    protected final PersistentStorage persistentStorage;
    protected final CredentialsRequest credentialsRequest;
    protected SebBalticsConfiguration configuration;
    private final String providerMarket;

    public SebBalticsApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            CredentialsRequest credentialsRequest,
            String providerMarket) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.credentialsRequest = credentialsRequest;
        this.providerMarket = providerMarket;
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
                        Keys.PSU_IP_ADDRESS,
                        credentialsRequest.getUserAvailability().getOriginatingUserIpOrDefault())
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

    public BalanceResponse fetchAccountBalances(String accountId) {

        return createRequestInSession((Urls.BALANCES).parameter(IdTags.ACCOUNT_ID, accountId))
                .get(BalanceResponse.class);
    }

    protected RequestBuilder createRequestInSession(URL url) {
        RequestBuilder requestBuilder =
                createRequest(url)
                        .header(Keys.CONSENT_ID, persistentStorage.get(StorageKeys.USER_CONSENT_ID))
                        .addBearerToken(getTokenFromStorage());

        requestBuilder.header(
                HeaderKeys.PSU_INVOLVED, credentialsRequest.getUserAvailability().isUserPresent());

        final Credentials credentials = credentialsRequest.getCredentials();

        requestBuilder
                .header(Keys.PSU_ID, credentials.getField(Key.USERNAME))
                .header(
                        HeaderKeys.PSU_CORPORATE_ID,
                        getFormattedPsuCorporateId(credentials.getField(Key.CORPORATE_ID)));

        return requestBuilder;
    }

    protected String getFormattedPsuCorporateId(String psuCorporateId) {

        // In LV the format used is DDMMYY-XNNNZ
        if (providerMarket.equals("LV")) {
            StringBuilder stringBuilder = new StringBuilder(psuCorporateId);
            stringBuilder.insert(6, '-');
            return stringBuilder.toString();
        }
        // In EE and LT, no formatting needed
        return psuCorporateId;
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
                .header(Keys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(Keys.DATE, getLocalDateTime());
    }
}
