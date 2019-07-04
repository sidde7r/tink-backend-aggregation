package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.DemoFinancialInstitutionConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.authenticator.DemoFinancialInstitutionAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.configuration.DemoFinancialInstitutionConfiguration;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.fetcher.creditcard.DemoFinancialInstitutionCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.fetcher.identity.DemoFinancialInstitutionIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.fetcher.loan.DemoFinancialInstitutionLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.fetcher.transactionalaccount.DemoFinancialInstitutionTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.sessionhandler.DemoFinancialInstitutionSessionHandler;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

/* This is the agent for the Demo Financial Institution which is a Tink developed test & demo bank */

public final class DemoFinancialInstitutionAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor {

    private final String clientName;
    private final DemoFinancialInstitutionApiClient apiClient;
    private final SessionStorage sessionStorage;

    public DemoFinancialInstitutionAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        clientName = request.getProvider().getPayload();
        sessionStorage = new SessionStorage();
        apiClient = new DemoFinancialInstitutionApiClient(client, sessionStorage);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        apiClient.setConfiguration(getClientConfiguration());
    }

    public DemoFinancialInstitutionConfiguration getClientConfiguration() {
        return configuration
                .getIntegrations()
                .getClientConfiguration(
                        DemoFinancialInstitutionConstants.INTEGRATION_NAME,
                        clientName,
                        DemoFinancialInstitutionConfiguration.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new DemoFinancialInstitutionAuthenticator(sessionStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        final DemoFinancialInstitutionTransactionalAccountFetcher transactionalAccountsFetcher =
                new DemoFinancialInstitutionTransactionalAccountFetcher(apiClient, sessionStorage);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        transactionalAccountsFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(
                                        transactionalAccountsFetcher))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        final DemoFinancialInstitutionCreditCardFetcher creditCardFetcher =
                new DemoFinancialInstitutionCreditCardFetcher(apiClient, sessionStorage);

        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        creditCardFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(creditCardFetcher))));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        final DemoFinancialInstitutionLoanFetcher loanFetcher =
                new DemoFinancialInstitutionLoanFetcher(apiClient, sessionStorage);

        return Optional.of(
                new LoanRefreshController(metricRefreshController, updateController, loanFetcher));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new DemoFinancialInstitutionSessionHandler(apiClient);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        final IdentityDataFetcher fetcher = new DemoFinancialInstitutionIdentityDataFetcher();
        return new FetchIdentityDataResponse(fetcher.fetchIdentityData());
    }
}
