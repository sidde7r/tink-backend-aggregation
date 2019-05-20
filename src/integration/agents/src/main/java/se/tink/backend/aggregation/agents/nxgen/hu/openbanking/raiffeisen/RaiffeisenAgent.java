package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.RaiffeisenConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.RaiffeisenConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.RaiffeisenConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.authenticator.RaiffeisenAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.configuration.RaiffeisenConfiguration;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.fetcher.transactionalaccount.RaiffeiesenTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.fetcher.transactionalaccount.RaiffeisenAccountFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
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

public final class RaiffeisenAgent extends NextGenerationAgent {
    private final RaiffeisenApiClient apiClient;

    public RaiffeisenAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new RaiffeisenApiClient(client, sessionStorage);
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final RaiffeisenConfiguration raiffeisenConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                Market.INTEGRATION_NAME,
                                request.getProvider().getPayload(),
                                RaiffeisenConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                ErrorMessages.MISSING_CONFIGURATION));
        apiClient.setConfiguration(raiffeisenConfiguration);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        RaiffeisenAuthenticator authenticator =
                new RaiffeisenAuthenticator(
                        apiClient, sessionStorage, credentials.getField(CredentialKeys.IBAN));
        OAuth2AuthenticationController oAuth2AuthenticationController =
                new OAuth2AuthenticationController(
                        persistentStorage, supplementalInformationHelper, authenticator);
        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        oAuth2AuthenticationController, supplementalInformationHelper),
                oAuth2AuthenticationController);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new RaiffeisenAccountFetcher(apiClient),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionPagePaginationController<>(
                                        new RaiffeiesenTransactionFetcher(apiClient), 0))));
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
