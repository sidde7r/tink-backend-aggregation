package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor;

import java.util.Optional;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.SparebankenSorAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.SparebankenSorMultiFactorAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.FirstLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.creditcard.SparebankenSorCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.creditcard.SparebankenSorCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.loan.SparebankenSorLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.SparebankenSorTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.SparebankenSorTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.filters.AddRefererFilter;
import se.tink.backend.aggregation.agents.utils.authentication.encap.EncapClient;
import se.tink.backend.aggregation.agents.utils.encoding.messagebodywriter.NoEscapeOfBackslashMessageBodyWriter;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticationControllerNO;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

/**
 * WIP! This provider is dependant on us being able to trigger supplemental information twice, which
 * doesn't work with the current version of the app. Haven't added the provider to the provider
 * config just to be certain that it doesn't accidentally end up in the app.
 *
 * <p>Things left to do before it can be used in production: - Assert that registration works
 * (activation flow) - Assert that registered user can log in (authentication flow) - Investigate
 * investment fetching, 2018-02-13 they seemed to route to the netbank - Add provider to provider
 * config - Add rules to appstore monitor
 */
public class SparebankenSorAgent extends NextGenerationAgent
        implements RefreshLoanAccountsExecutor {
    private final SparebankenSorApiClient apiClient;
    private final LoanRefreshController loanRefreshController;

    public SparebankenSorAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        apiClient = new SparebankenSorApiClient(client, sessionStorage);

        SparebankenSorLoanFetcher loanFetcher = new SparebankenSorLoanFetcher(apiClient);
        loanRefreshController =
                new LoanRefreshController(metricRefreshController, updateController, loanFetcher);
    }

    protected void configureHttpClient(TinkHttpClient client) {

        AddRefererFilter filter = new AddRefererFilter();
        client.addFilter(filter);
        client.addMessageWriter(new NoEscapeOfBackslashMessageBodyWriter(FirstLoginRequest.class));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        SparebankenSorEncapConfiguration configuration = new SparebankenSorEncapConfiguration();
        EncapClient encapClient =
                new EncapClient(
                        configuration,
                        persistentStorage,
                        client,
                        true,
                        credentials.getField(Field.Key.USERNAME));

        SparebankenSorMultiFactorAuthenticator multiFactorAuthenticator =
                new SparebankenSorMultiFactorAuthenticator(
                        apiClient,
                        encapClient,
                        supplementalInformationHelper,
                        catalog,
                        sessionStorage,
                        credentials.getField(Field.Key.MOBILENUMBER));

        SparebankenSorAutoAuthenticator autoAuthenticator =
                new SparebankenSorAutoAuthenticator(apiClient, encapClient, sessionStorage);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new BankIdAuthenticationControllerNO(
                        supplementalRequester, multiFactorAuthenticator),
                autoAuthenticator);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new SparebankenSorTransactionalAccountFetcher(apiClient),
                        new SparebankenSorTransactionFetcher(apiClient)));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        SparebankenSorCreditCardAccountFetcher ccAccountFetcher =
                new SparebankenSorCreditCardAccountFetcher(apiClient);
        SparebankenSorCreditCardTransactionFetcher ccTransactionFetcher =
                new SparebankenSorCreditCardTransactionFetcher();

        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        ccAccountFetcher,
                        ccTransactionFetcher));
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
    protected SessionHandler constructSessionHandler() {
        return new SparebankenSorSessionHandler();
    }
}
