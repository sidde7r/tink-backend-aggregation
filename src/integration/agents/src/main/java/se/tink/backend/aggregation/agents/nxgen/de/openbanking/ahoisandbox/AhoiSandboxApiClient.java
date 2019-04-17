package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.AhoiSandboxConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.AhoiSandboxConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.AhoiSandboxConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.AhoiSandboxConstants.UrlParameters;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.AhoiSandboxConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.entity.AccessFieldsEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.entity.AccessRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.rpc.BankingTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.rpc.BankingTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.rpc.CreateAccessResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.rpc.ProviderDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.rpc.ProvidersListRsponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.rpc.RegistrationTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.rpc.UserRegistrationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.configuration.AhoiSandboxConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class AhoiSandboxApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private AhoiSandboxConfiguration configuration;

    public AhoiSandboxApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    private AhoiSandboxConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(AhoiSandboxConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final String accessToken = getAccessTokenFromStorage();

        return createRequest(url)
                .header(
                        AhoiSandboxConstants.HeaderKeys.AUTHORIZATION,
                        AhoiSandboxConstants.HeaderValues.BEARER_PREFIX + accessToken);
    }

    private String getAccessTokenFromStorage() {
        return persistentStorage.get(AhoiSandboxConstants.StorageKeys.ACCESS_TOKEN);
    }

    private String getAccountIdFromStorage() {
        return persistentStorage.get(AhoiSandboxConstants.StorageKeys.ACCOUNT_ID);
    }

    public void createAccess(Credentials credentials, ProviderDetailsResponse providerDetailsResponse) {

        final CreateAccessResponse createAccessResponse =
                getCreateAcccessResponse(providerDetailsResponse.getId(), credentials);

        persistentStorage.put(
                AhoiSandboxConstants.StorageKeys.ACCOUNT_ID, createAccessResponse.getId());
    }

    private CreateAccessResponse getCreateAcccessResponse(
            String providerId, Credentials credentials) {

        final AccessRequestEntity accessRequestEntity =
                new AccessRequestEntity(
                        AhoiSandboxConstants.Forms.ACCESS_TYPE,
                        providerId,
                        new AccessFieldsEntity(
                                credentials.getField(Key.USERNAME),
                                credentials.getField(Key.PASSWORD)));

        return createRequestInSession(Urls.CREATE_ACCESS)
                .post(CreateAccessResponse.class, accessRequestEntity);
    }

    public ProviderDetailsResponse getProviderDetailsResponse(String providerId) {

        return createRequestInSession(new URL(Urls.PROVIDERS + providerId))
                .get(ProviderDetailsResponse.class);
    }

    public ProvidersListRsponse getProviderEntities() {
        return createRequestInSession(Urls.PROVIDERS).get(ProvidersListRsponse.class);
    }

    public BankingTokenResponse getBankingTokenResponse(String installationId) {

        final BankingTokenRequest bankingTokenRequest =
                new BankingTokenRequest(
                        installationId, UUID.randomUUID().toString(), Instant.now().toString());

        return createRequest(Urls.OAUTH)
                .queryParam(QueryKeys.GRANT_TYPE, QueryValues.GRANT_TYPE)
                .addBasicAuth(configuration.getClientId(), configuration.getClientSecret())
                .header(
                        AhoiSandboxConstants.HeaderKeys.X_AUTHORIZATION_AHOI,
                        bankingTokenRequest.toBase64Header())
                .post(BankingTokenResponse.class);
    }

    public UserRegistrationResponse getUserRegistrationResponse(String registrationAccessToken) {

        return createRequest(Urls.REGISTRATION)
                .header(
                        AhoiSandboxConstants.HeaderKeys.AUTHORIZATION,
                        AhoiSandboxConstants.HeaderValues.BEARER_PREFIX + registrationAccessToken)
                .post(UserRegistrationResponse.class);
    }

    public RegistrationTokenResponse getRegistrationTokenResponse() {

        return createRequest(Urls.OAUTH)
                .queryParam(QueryKeys.GRANT_TYPE, QueryValues.GRANT_TYPE)
                .addBasicAuth(configuration.getClientId(), configuration.getClientSecret())
                .post(RegistrationTokenResponse.class);
    }

    public AccountsResponse fetchAccounts() {
        return createRequestInSession(
                        Urls.ACCOUNTS.parameter(
                                AhoiSandboxConstants.UrlParameters.ACCESS_ID,
                                getAccountIdFromStorage()))
                .get(AccountsResponse.class);
    }

    public TransactionsResponse fetchTransactions(TransactionalAccount account) {
        return createRequestInSession(
                        Urls.TRANSACTIONS
                                .parameter(UrlParameters.ACCESS_ID, getAccountIdFromStorage())
                                .parameter(UrlParameters.ACCOUNT_ID, account.getApiIdentifier()))
                .get(TransactionsResponse.class);
    }
}
