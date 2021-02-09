package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.HandelsbankenAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.loan.HandelsbankenLoanFetcher;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class HandelsbankenAgent<
                API extends HandelsbankenApiClient, Config extends HandelsbankenConfiguration>
        extends NextGenerationAgent implements RefreshLoanAccountsExecutor {

    protected final API bankClient;
    private final HandelsbankenPersistentStorage handelsbankenPersistentStorage;
    protected final HandelsbankenSessionStorage handelsbankenSessionStorage;
    private final Config handelsbankenConfiguration;

    private final LoanRefreshController loanRefreshController;

    public HandelsbankenAgent(
            AgentComponentProvider agentComponentProvider, Config handelsbankenConfiguration) {
        super(agentComponentProvider);
        this.handelsbankenConfiguration = handelsbankenConfiguration;
        this.handelsbankenPersistentStorage =
                new HandelsbankenPersistentStorage(
                        this.persistentStorage, credentials.getSensitivePayloadAsMap());
        this.bankClient = constructApiClient(handelsbankenConfiguration);
        this.handelsbankenSessionStorage =
                new HandelsbankenSessionStorage(
                        handelsbankenConfiguration, this.sessionStorage, context.getLogMasker());

        this.loanRefreshController =
                new LoanRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new HandelsbankenLoanFetcher(
                                this.bankClient,
                                this.handelsbankenSessionStorage,
                                this.credentials));
    }

    public HandelsbankenAgent(
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair,
            Config handelsbankenConfiguration) {
        super(request, context, signatureKeyPair);
        this.handelsbankenConfiguration = handelsbankenConfiguration;
        this.handelsbankenPersistentStorage =
                new HandelsbankenPersistentStorage(
                        this.persistentStorage, credentials.getSensitivePayloadAsMap());
        this.bankClient = constructApiClient(handelsbankenConfiguration);
        this.handelsbankenSessionStorage =
                new HandelsbankenSessionStorage(
                        handelsbankenConfiguration, this.sessionStorage, context.getLogMasker());

        this.loanRefreshController =
                new LoanRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new HandelsbankenLoanFetcher(
                                this.bankClient,
                                this.handelsbankenSessionStorage,
                                this.credentials));
    }

    protected abstract API constructApiClient(Config handelsbankenConfiguration);

    @Override
    protected Authenticator constructAuthenticator() {
        return new TypedAuthenticationController(
                constructAuthenticators(
                        this.bankClient,
                        this.handelsbankenConfiguration,
                        this.handelsbankenPersistentStorage,
                        this.handelsbankenSessionStorage));
    }

    protected abstract TypedAuthenticator[] constructAuthenticators(
            API bankClient,
            Config handelsbankenConfiguration,
            HandelsbankenPersistentStorage handelsbankenPersistentStorage,
            HandelsbankenSessionStorage handelsbankenSessionStorage);

    protected AutoAuthenticationController constructAutoAuthenticationController(
            TypedAuthenticator cardDeviceAuthenticator,
            HandelsbankenAutoAuthenticator autoAuthenticator) {
        return new AutoAuthenticationController(
                this.request, this.systemUpdater, cardDeviceAuthenticator, autoAuthenticator);
    }

    protected AutoAuthenticationController constructAutoAuthenticationController(
            TypedAuthenticator cardDeviceAuthenticator) {
        return constructAutoAuthenticationController(
                cardDeviceAuthenticator, constructAutoAuthenticator());
    }

    protected HandelsbankenAutoAuthenticator constructAutoAuthenticator() {
        return new HandelsbankenAutoAuthenticator(
                this.bankClient,
                this.handelsbankenPersistentStorage,
                this.credentials,
                this.handelsbankenSessionStorage,
                this.handelsbankenConfiguration);
    }

    protected abstract Optional<TransferController> constructTransferController(
            API client, HandelsbankenSessionStorage sessionStorage, CompositeAgentContext context);

    @Override
    protected SessionHandler constructSessionHandler() {
        return new HandelsbankenSessionHandler(
                this.bankClient,
                this.handelsbankenPersistentStorage,
                this.credentials,
                this.handelsbankenSessionStorage);
    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return loanRefreshController.fetchLoanTransactions();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return constructTransferController(
                this.bankClient, this.handelsbankenSessionStorage, this.context);
    }
}
