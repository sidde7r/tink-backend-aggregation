package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.OpenBankProjectConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.authenticator.OpenBankProjectAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.configuration.OpenBankProjectConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.OpenBankProjectTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.OpenBankProjectTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class OpenBankProjectAgent extends NextGenerationAgent {
    private final OpenBankProjectApiClient apiClient;

    public OpenBankProjectAgent(
            final CredentialsRequest request,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new OpenBankProjectApiClient(client, sessionStorage);
    }

    public abstract String getIntegrationName();

    public abstract String getClientName();

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final OpenBankProjectConfiguration openBankProjectConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                getIntegrationName(),
                                getClientName(),
                                OpenBankProjectConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                ErrorMessages.MISSING_CONFIGURATION));
        if (!openBankProjectConfiguration.isValid()) {
            throw new IllegalStateException(ErrorMessages.BAD_CONFIGURATION);
        }

        apiClient.setConfiguration(openBankProjectConfiguration);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OpenBankProjectAuthenticator authenticator =
                new OpenBankProjectAuthenticator(apiClient, sessionStorage);
        return new PasswordAuthenticationController(authenticator);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        final OpenBankProjectTransactionalAccountFetcher accountFetcher =
                new OpenBankProjectTransactionalAccountFetcher(apiClient);

        final OpenBankProjectTransactionFetcher transactionFetcher =
                new OpenBankProjectTransactionFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionPagePaginationController<>(
                                        transactionFetcher,
                                        OpenBankProjectConstants.Fetcher.START_PAGE))));
    }

    @Override
    public Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<TransferDestinationRefreshController>
            constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
