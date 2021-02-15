package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc;

import java.lang.invoke.MethodHandles;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.PersistentStorageService;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.configuration.KbcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupAgentPlatformStorageApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.BerlinGroupAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class KbcApiClient extends BerlinGroupAgentPlatformStorageApiClient<KbcConfiguration> {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private KbcAuthenticationData authData;

    public KbcApiClient(
            final TinkHttpClient client,
            final KbcConfiguration configuration,
            final CredentialsRequest request,
            final String redirectUrl,
            final PersistentStorage persistentStorage,
            final String qSealc) {
        super(client, persistentStorage, configuration, request, redirectUrl, qSealc);
    }

    @Override
    public BerlinGroupAccountResponse fetchAccounts() {
        return getAccountsRequestBuilder(getConfiguration().getBaseUrl() + Urls.ACCOUNTS)
                .header(Psd2Headers.Keys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(Psd2Headers.Keys.PSU_IP_ADDRESS, getConfiguration().getPsuIpAddress())
                .get(AccountResponse.class);
    }

    @Override
    public TransactionsKeyPaginatorBaseResponse fetchTransactions(String url) {
        logger.info("Fetching transactions endpoint requested: {}", url);
        return getTransactionsRequestBuilder(url)
                .header(BerlinGroupConstants.HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .get(TransactionsKeyPaginatorBaseResponse.class);
    }

    @Override
    public RequestBuilder getTransactionsRequestBuilder(final String url) {
        return client.request(url)
                .addBearerToken(getTokenFromSession(StorageKeys.OAUTH_TOKEN))
                .header(
                        BerlinGroupConstants.HeaderKeys.PSU_IP_ADDRESS,
                        getConfiguration().getPsuIpAddress())
                .header(BerlinGroupConstants.HeaderKeys.CONSENT_ID, getConsentIdFromStorage());
    }

    @Override
    protected String getConsentIdFromStorage() {
        return getKbcPersistedData().getConsentId();
    }

    private KbcAuthenticationData getKbcPersistedData() {

        if (authData == null) {
            authData =
                    new KbcPersistedDataAccessorFactory(new ObjectMapperFactory().getInstance())
                            .createKbcAuthenticationPersistedDataAccessor(
                                    new PersistentStorageService(persistentStorage)
                                            .readFromAgentPersistentStorage())
                            .getKbcAuthenticationData();
        }
        return authData;
    }

    /**
     * Those methods are not used in Agent Platform flow thus null is returned just to fill method
     * body.
     */
    @Override
    public OAuth2Token getToken(final String code) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OAuth2Token refreshToken(final String token) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getConsentId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL getAuthorizeUrl(String state) {
        throw new UnsupportedOperationException();
    }
}
