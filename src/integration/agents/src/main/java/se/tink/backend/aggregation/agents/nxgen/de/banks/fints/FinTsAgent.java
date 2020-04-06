package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.authenticator.FinTsAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.Bank;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.FinTsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.FinTsSecretsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.PayloadParser;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.fetcher.transactionalaccount.FinTsAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.fetcher.transactionalaccount.FinTsTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice.TanAnswerProvider;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.session.FinTsSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class FinTsAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final FinTsDialogContext dialogContext;

    public FinTsAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        CredentialsRequest request = componentProvider.getCredentialsRequest();
        PayloadParser.Payload payload = PayloadParser.parse(request.getProvider().getPayload());
        FinTsConfiguration configuration =
                new FinTsConfiguration(
                        payload.getBlz(),
                        Bank.of(payload.getBankName()),
                        payload.getEndpoint(),
                        request.getCredentials().getField(Field.Key.USERNAME),
                        request.getCredentials().getField(Field.Key.PASSWORD));
        FinTsSecretsConfiguration secretsConfiguration = getSecretsConfiguration();
        this.dialogContext = new FinTsDialogContext(configuration, secretsConfiguration);

        TanAnswerProvider tanAnswerProvider =
                new TanAnswerProvider(componentProvider.getSupplementalInformationHelper());
        FinTsRequestSender requestSender =
                new FinTsRequestSender(
                        componentProvider.getTinkHttpClient(),
                        dialogContext.getConfiguration().getEndpoint());
        FinTsRequestProcessor requestProcessor =
                new FinTsRequestProcessor(this.dialogContext, requestSender, tanAnswerProvider);

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    private FinTsSecretsConfiguration getSecretsConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(FinTsSecretsConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(new FinTsAuthenticator());
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
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new FinTsAccountFetcher(),
                new FinTsTransactionFetcher());
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new FinTsSessionHandler();
    }
}
