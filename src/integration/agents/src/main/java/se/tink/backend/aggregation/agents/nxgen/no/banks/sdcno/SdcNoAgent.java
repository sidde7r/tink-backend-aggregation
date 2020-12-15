package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno;

import java.util.Date;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.SdcNoBankIdSSAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConfiguration;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.account.SdcNoAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.account.SdcNoTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.account.SdcNoTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.converter.DefaultAccountNumberToIbanConverter;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SdcNoAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {
    protected final SdcNoConfiguration configuration;
    protected final SdcNoApiClient bankClient;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public SdcNoAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        configuration = new SdcNoConfiguration(request.getProvider());
        bankClient = new SdcNoApiClient(client, configuration);

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();

        client.addFilter(new PermanentRedirectFilter());
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                this.metricRefreshController,
                this.updateController,
                new SdcNoAccountFetcher(
                        bankClient, DefaultAccountNumberToIbanConverter.NO_CONVERTER),
                new SdcNoTransactionFetcher(bankClient, new SdcNoTransactionParser(), Date::new));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new SdcNoBankIdSSAuthenticator(
                configuration, client, supplementalRequester, catalog);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
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
}
