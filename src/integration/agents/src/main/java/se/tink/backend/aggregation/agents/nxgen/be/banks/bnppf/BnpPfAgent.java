package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.authenticator.BnpPfAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.fetcher.BnpPfTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.filter.BnpPfHttpFilter;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class BnpPfAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private static final String BNPPF_CERT_PATH = "/etc/tink/bnppf-cert.p12";

    private final BnpPfApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public BnpPfAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        apiClient = new BnpPfApiClient(client);
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    protected void configureHttpClient(TinkHttpClient client) {
        try {
            byte[] clientCertificateBytes =
                    FileUtils.readFileToByteArray(new File(BNPPF_CERT_PATH));
            client.setSslClientCertificate(clientCertificateBytes, "");
            client.addFilter(new BnpPfHttpFilter(credentials.getField(Field.Key.ACCESS_TOKEN)));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BnpPfAuthenticator();
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        BnpPfTransactionalAccountFetcher transactionalAccountFetcher =
                new BnpPfTransactionalAccountFetcher(apiClient);
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                transactionalAccountFetcher,
                transactionalAccountFetcher);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
