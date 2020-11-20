package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import java.security.cert.CertificateException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.SparebankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.SparebankController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.configuration.SparebankApiConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.SparebankAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.SparebankTransactionFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class SparebankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final SparebankApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public SparebankAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());

        apiClient =
                new SparebankApiClient(
                        client,
                        persistentStorage,
                        agentsServiceConfiguration.getEidasProxy(),
                        this.getEidasIdentity(),
                        getApiConfiguration());
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    public AgentConfiguration<SparebankConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(SparebankConfiguration.class);
    }

    private SparebankApiConfiguration getApiConfiguration() {
        try {
            AgentConfiguration<SparebankConfiguration> agentConfiguration = getAgentConfiguration();
            String qsealcBase64 =
                    CertificateUtils.getDerEncodedCertFromBase64EncodedCertificate(
                            agentConfiguration.getQsealc());

            return SparebankApiConfiguration.builder()
                    .baseUrl(splitPayload(request.getProvider().getPayload()).get(1))
                    .redirectUrl(agentConfiguration.getRedirectUrl())
                    .qsealcBase64(qsealcBase64)
                    .certificateIssuerDN(CertificateUtils.getCertificateIssuerDN(qsealcBase64))
                    .certificateSerialNumberInHex(
                            CertificateUtils.getSerialNumber(qsealcBase64, 16))
                    .userIp(userIp)
                    .isManual(request.isManual())
                    .build();
        } catch (CertificateException e) {
            throw new IllegalStateException(
                    "Cannot create api configuration due to certificate parsing error", e);
        }
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final SparebankController controller =
                new SparebankController(
                        supplementalInformationHelper,
                        new SparebankAuthenticator(apiClient),
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
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

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final SparebankAccountFetcher accountFetcher = new SparebankAccountFetcher(apiClient);

        final SparebankTransactionFetcher transactionFetcher =
                new SparebankTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    private List<String> splitPayload(String payload) {
        return Stream.of(payload.split(SparebankConstants.REGEX)).collect(Collectors.toList());
    }
}
