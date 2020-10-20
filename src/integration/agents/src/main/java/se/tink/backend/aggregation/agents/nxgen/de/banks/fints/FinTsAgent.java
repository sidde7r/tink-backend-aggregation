package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.authenticator.FinTsAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.account.AccountClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.dialog.DialogClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.transaction.TransactionClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.Bank;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.FinTsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.FinTsSecretsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.PayloadParser;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.fetcher.transactionalaccount.FinTsAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.fetcher.transactionalaccount.FinTsTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.account.DefaultHisalBalance;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.account.FinTsTransactionalAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.account.HisalBalance;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.account.IngHisalBalance;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.accounttype.FinTsAccountTypeMapper;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.transaction.FinTsTransactionMapper;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice.ChosenSecurityFunctionProvider;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice.ChosenTanMediumProvider;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice.TanAnswerProvider;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class FinTsAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final DialogClient dialogClient;
    private final TransactionClient transactionClient;
    private final AccountClient accountClient;
    private final FinTsDialogContext dialogContext;

    @Inject
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
        FinTsSecretsConfiguration secretsConfiguration =
                new FinTsSecretsConfiguration(
                        FinTsConstants.PRODUCT_ID, FinTsConstants.PRODUCT_VERSION);
        this.dialogContext = new FinTsDialogContext(configuration, secretsConfiguration);

        TanAnswerProvider tanAnswerProvider =
                new TanAnswerProvider(
                        componentProvider.getSupplementalInformationHelper(), catalog);
        FinTsRequestSender requestSender =
                new FinTsRequestSender(
                        componentProvider.getTinkHttpClient(),
                        dialogContext.getConfiguration().getEndpoint());
        FinTsRequestProcessor requestProcessor =
                new FinTsRequestProcessor(this.dialogContext, requestSender, tanAnswerProvider);

        this.dialogClient =
                new DialogClient(
                        requestProcessor,
                        dialogContext,
                        new ChosenTanMediumProvider(
                                componentProvider.getSupplementalInformationHelper(), catalog),
                        new FinTsAccountTypeMapper(),
                        new ChosenSecurityFunctionProvider(supplementalInformationHelper, catalog));
        this.transactionClient = new TransactionClient(requestProcessor, dialogContext);
        this.accountClient = new AccountClient(requestProcessor, dialogContext);

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(new FinTsAuthenticator(dialogClient));
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
                new FinTsAccountFetcher(
                        dialogContext,
                        accountClient,
                        new FinTsTransactionalAccountMapper(getHisalBalance())),
                new FinTsTransactionFetcher(
                        dialogContext, transactionClient, new FinTsTransactionMapper()));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    private HisalBalance getHisalBalance() {
        Bank bank = dialogContext.getConfiguration().getBank();
        return bank.equals(Bank.ING_DIBA) ? new IngHisalBalance() : new DefaultHisalBalance();
    }
}
