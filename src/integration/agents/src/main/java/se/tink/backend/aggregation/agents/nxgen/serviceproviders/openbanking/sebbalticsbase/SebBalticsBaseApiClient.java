package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsCommonConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsCommonConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsCommonConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsCommonConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.AuthMethodSelectionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.ConsentAuthMethod;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.ConsentAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.DecoupledAuthMethod;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.DecoupledAuthRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.DecoupledAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.DecoupledTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.configuration.SebBlaticsConfiguration;
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

public abstract class SebBalticsBaseApiClient {

    protected final TinkHttpClient client;
    protected final PersistentStorage persistentStorage;
    protected final CredentialsRequest credentialsRequest;
    protected SebBlaticsConfiguration configuration;

    public SebBalticsBaseApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            CredentialsRequest credentialsRequest) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.credentialsRequest = credentialsRequest;
    }

    public void setConfiguration(SebBlaticsConfiguration configuration) {
        this.configuration = configuration;
    }

    public DecoupledAuthResponse startDecoupledAuthorization(
            DecoupledAuthRequest authorizationRequest) {
        return client.request(Urls.DECOUPLED_AUTHORIZATION)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(authorizationRequest, MediaType.APPLICATION_JSON_TYPE)
                .header(HeaderKeys.CLIENT_ID, configuration.getClientId())
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.DATE, getLocalDateTime())
                .post(DecoupledAuthResponse.class);
    }

    public DecoupledAuthResponse getDecoupledAuthStatus(String authRequestId) {
        return client.request(
                        Urls.DECOUPLED_AUTHORIZATION
                                .concat(URL.URL_SEPARATOR)
                                .concat(authRequestId))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header(HeaderKeys.CLIENT_ID, configuration.getClientId())
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.DATE, getLocalDateTime())
                .get(DecoupledAuthResponse.class);
    }

    public AuthMethodSelectionResponse updateDecoupledAuthStatus(
            DecoupledAuthMethod decoupledAuthMethod, String authRequestId) {
        return client.request(
                        Urls.DECOUPLED_AUTHORIZATION
                                .concat(URL.URL_SEPARATOR)
                                .concat(authRequestId))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(decoupledAuthMethod, MediaType.APPLICATION_JSON_TYPE)
                .header(HeaderKeys.CLIENT_ID, configuration.getClientId())
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.DATE, getLocalDateTime())
                .patch(AuthMethodSelectionResponse.class);
    }

    public TokenResponse getDecoupledToken(DecoupledTokenRequest tokenRequest) {
        return client.request(Urls.DECOUPLED_TOKEN)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(tokenRequest, MediaType.APPLICATION_JSON_TYPE)
                .header(HeaderKeys.CLIENT_ID, configuration.getClientId())
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.DATE, getLocalDateTime())
                .post(TokenResponse.class);
    }

    public ConsentResponse createNewConsent(ConsentRequest consentRequest) {
        return client.request(Urls.NEW_CONSENT)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(consentRequest, MediaType.APPLICATION_JSON_TYPE)
                .header(HeaderKeys.CLIENT_ID, configuration.getClientId())
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.DATE, getLocalDateTime())
                .header(HeaderKeys.PSU_IP_ADDRESS, SebBalticsCommonConstants.getPsuIpAddress())
                .addBearerToken(getTokenFromStorage())
                .post(ConsentResponse.class);
    }

    public ConsentAuthorizationResponse startConsentAuthorization(String consentId) {
        return client.request(
                        new URL(Urls.CONSENT_AUTHORIZATION).parameter(IdTags.CONSENT_ID, consentId))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header(HeaderKeys.CLIENT_ID, configuration.getClientId())
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.DATE, getLocalDateTime())
                .addBearerToken(getTokenFromStorage())
                .post(ConsentAuthorizationResponse.class);
    }

    public AuthMethodSelectionResponse updateConsentAuthorization(
            ConsentAuthMethod consentAuthMethod, String authorizationId, String consentId) {

        return client.request(
                        new URL(Urls.CONSENT_AUTHORIZATION)
                                .parameter(IdTags.CONSENT_ID, consentId)
                                .concat(URL.URL_SEPARATOR)
                                .concat(authorizationId))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(consentAuthMethod, MediaType.APPLICATION_JSON_TYPE)
                .header(HeaderKeys.CLIENT_ID, configuration.getClientId())
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.DATE, getLocalDateTime())
                .addBearerToken(getTokenFromStorage())
                .patch(AuthMethodSelectionResponse.class);
    }

    public ConsentResponse getConsentStatus(String consentId) {
        return client.request(new URL(Urls.CONSENT_STATUS).parameter(IdTags.CONSENT_ID, consentId))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header(HeaderKeys.CLIENT_ID, configuration.getClientId())
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.DATE, getLocalDateTime())
                .addBearerToken(getTokenFromStorage())
                .get(ConsentResponse.class);
    }

    // fetch list of PSU's accounts without the consent
    public AccountsResponse fetchAccountsList() {
        return client.request(Urls.ACCOUNTS_LIST)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header(HeaderKeys.CLIENT_ID, configuration.getClientId())
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.DATE, getLocalDateTime())
                .addBearerToken(getTokenFromStorage())
                .get(AccountsResponse.class);
    }

    // fetch PSU's accounts under a given PSU consent
    public AccountsResponse fetchAccounts() {
        return createRequestInSession(new URL(Urls.BASE_URL).concat(Urls.ACCOUNTS))
                .get(AccountsResponse.class);
    }

    public TransactionsResponse fetchTransactions(URL urlAddress) {

        // URL url = new URL(SebCommonConstants.Urls.BASE_URL).concat(urlAddress);
        RequestBuilder requestBuilder = createRequestInSession(urlAddress);

        return requestBuilder.get(TransactionsResponse.class);
    }

    public TransactionsResponse fetchTransactions(String accountId, LocalDate from, LocalDate to) {

        return createRequestInSession(
                        new URL(Urls.TRANSACTIONS).parameter(IdTags.ACCOUNT_ID, accountId))
                .queryParam(QueryKeys.DATE_FROM, from.toString())
                .queryParam(QueryKeys.DATE_TO, to.toString())
                .get(TransactionsResponse.class);
    }

    public BalanceResponse fetchAccountBalances(String accountId) {

        return createRequestInSession(
                        new URL(Urls.BALANCES).parameter(IdTags.ACCOUNT_ID, accountId))
                .get(BalanceResponse.class);
    }

    protected RequestBuilder createRequestInSession(URL url) {
        RequestBuilder requestBuilder =
                createRequest(url)
                        .header(HeaderKeys.CLIENT_ID, configuration.getClientId())
                        .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                        .header(HeaderKeys.DATE, getLocalDateTime())
                        .header(HeaderKeys.CONSENT_ID, persistentStorage.get("USER_CONSENT_ID"))
                        .addBearerToken(getTokenFromStorage());

        if (credentialsRequest.getUserAvailability().isUserPresent()) {
            requestBuilder.header(HeaderKeys.PSU_INVOLVED, true);
        } else {
            requestBuilder.header(HeaderKeys.PSU_INVOLVED, false);
        }

        final Credentials credentials = credentialsRequest.getCredentials();

        if (credentials.hasField(Key.USERNAME)) {
            String psuId = credentials.getField(Key.USERNAME);
            if (Strings.isNullOrEmpty(psuId)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            } else {
                requestBuilder.header(HeaderKeys.PSU_Id, psuId);
            }
        }

        if (credentials.hasField(Key.CORPORATE_ID)) {
            String psuCorporateId = credentials.getField(Key.CORPORATE_ID);
            if (Strings.isNullOrEmpty(psuCorporateId)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            } else {
                requestBuilder.header(HeaderKeys.PSU_CORPORATE_ID, psuCorporateId);
            }
        }

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
                .type(MediaType.APPLICATION_JSON);
    }
}
