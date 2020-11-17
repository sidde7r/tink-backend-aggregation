package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.IngAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.crypto.IngCryptoUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.IngTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.IngTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.IngLoggingAdapter;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.IngRequestFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.ProxyFilter;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.log.executor.LoggingExecutor;
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
            AgentComponentProvider agentComponentProvider,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            TinkHttpClient httpClient) {
        this.ingCryptoUtils = new IngCryptoUtils();
        this.ingStorage = new IngStorage(persistentStorage, sessionStorage, ingCryptoUtils);
        LoggingExecutor loggingExecutor =
                new LoggingExecutor(
                        agentComponentProvider.getContext().getLogOutputStream(),
                        agentComponentProvider.getContext().getLogMasker(),
                        LogMaskerImpl.shouldLog(
                                agentComponentProvider.getCredentialsRequest().getProvider()));
        IngLoggingAdapter ingLoggingAdapter = new IngLoggingAdapter(loggingExecutor);
        this.ingDirectApiClient = new IngDirectApiClient(httpClient, ingLoggingAdapter);
        ProxyFilter proxyFilter = new ProxyFilter(ingStorage, ingCryptoUtils, ingLoggingAdapter);
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
