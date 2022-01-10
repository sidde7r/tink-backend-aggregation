package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.agent.sdk.operation.User;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.SparebankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.SparebankController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.configuration.SparebankApiConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.SparebankCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.SparebankCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.mapper.SparebankCardMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.mapper.SparebankCardTransactionMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.SparebankAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.SparebankTransactionFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TerminatedHandshakeRetryFilter;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
public final class SparebankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor {

    private final SparebankApiClient apiClient;
    private final SparebankStorage storage;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;

    @Inject
    public SparebankAgent(
            AgentComponentProvider agentComponentProvider, QsealcSigner qsealcSigner) {
        super(agentComponentProvider);

        client.addFilter(new TerminatedHandshakeRetryFilter());
        storage = new SparebankStorage(persistentStorage);
        apiClient =
                new SparebankApiClient(
                        client,
                        qsealcSigner,
                        getApiConfiguration(agentComponentProvider.getUser()),
                        storage);

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        creditCardRefreshController = getCreditCardRefreshController();
    }

    public AgentConfiguration<SparebankConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(SparebankConfiguration.class);
    }

    private SparebankApiConfiguration getApiConfiguration(User user) {
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
                    .userIp(user.getIpAddress())
                    .isUserPresent(user.isPresent())
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
                        new SparebankAuthenticator(
                                apiClient, storage, credentials, new ActualLocalDateTimeSource()),
                        strongAuthenticationState,
                        credentials);

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

    private CreditCardRefreshController getCreditCardRefreshController() {
        final SparebankCardFetcher cardFetcher =
                new SparebankCardFetcher(apiClient, new SparebankCardMapper());

        final SparebankCardTransactionFetcher transactionFetcher =
                new SparebankCardTransactionFetcher(
                        apiClient, new SparebankCardTransactionMapper());

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                cardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }
}
