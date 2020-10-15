package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.IngAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.crypto.IngCryptoUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.IngTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.IngTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.IngRequestFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.ProxyFilter;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@Getter
public class IngConfiguration {

    private final IngProxyApiClient ingProxyApiClient;
    private final IngDirectApiClient ingDirectApiClient;
    private final IngStorage ingStorage;
    private final IngCryptoUtils ingCryptoUtils;
    private final IngRequestFactory ingRequestFactory;

    public IngConfiguration(
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            TinkHttpClient httpClient) {
        this.ingCryptoUtils = new IngCryptoUtils();
        this.ingStorage = new IngStorage(persistentStorage, sessionStorage, ingCryptoUtils);
        this.ingDirectApiClient = new IngDirectApiClient(httpClient);
        ProxyFilter proxyFilter = new ProxyFilter(ingStorage, ingCryptoUtils);
        this.ingProxyApiClient = new IngProxyApiClient(httpClient, proxyFilter, ingStorage);
        this.ingRequestFactory = new IngRequestFactory(ingStorage);
    }

    public IngSessionHandler createSessionHandler() {
        return new IngSessionHandler(ingProxyApiClient);
    }

    public IngTransactionalAccountFetcher createAccountFetcher() {
        return new IngTransactionalAccountFetcher(ingProxyApiClient);
    }

    public IngTransactionFetcher createTransactionFetcher() {
        return new IngTransactionFetcher(ingProxyApiClient);
    }

    public IngAuthenticator createAuthenticator(
            SupplementalInformationFormer supplementalInformationFormer) {
        return new IngAuthenticator(this, supplementalInformationFormer);
    }
}
