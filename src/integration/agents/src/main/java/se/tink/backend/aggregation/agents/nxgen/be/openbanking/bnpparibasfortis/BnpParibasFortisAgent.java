package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.authenticator.BnpParibasFortisAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.configuration.BnpParibasFortisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.BnpParibasFortisTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.session.BnpParibasFortisSessionHandler;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class BnpParibasFortisAgent extends NextGenerationAgent {

    private final BnpParibasFortisApiClient apiClient;

    public BnpParibasFortisAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new BnpParibasFortisApiClient(client, sessionStorage, persistentStorage);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {}

    @Override
    protected Authenticator constructAuthenticator() {
        final BnpParibasFortisAuthenticator authenticator =
                new BnpParibasFortisAuthenticator(apiClient, sessionStorage);
        final OAuth2AuthenticationController oAuth2AuthenticationController =
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
        final BnpParibasFortisTransactionalAccountFetcher accountFetcher =
                new BnpParibasFortisTransactionalAccountFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(accountFetcher))));
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final BnpParibasFortisConfiguration bnpParibasFortisConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                Market.INTEGRATION_NAME,
                                Market.CLIENT_NAME,
                                BnpParibasFortisConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "BNP Paribas Fortis configuration missing."));

        persistentStorage.put(
                StorageKeys.AUTH_BASE_URL, bnpParibasFortisConfiguration.getAuthBaseUrl());
        persistentStorage.put(StorageKeys.CLIENT_ID, bnpParibasFortisConfiguration.getClientId());
        persistentStorage.put(
                StorageKeys.REDIRECT_URI, bnpParibasFortisConfiguration.getRedirectUri());
        persistentStorage.put(
                StorageKeys.CLIENT_SECRET, bnpParibasFortisConfiguration.getClientSecret());
        persistentStorage.put(
                StorageKeys.ORGANIZATION_ID, bnpParibasFortisConfiguration.getOrganisationId());
        persistentStorage.put(
                StorageKeys.OPENBANK_STET_VERSION,
                bnpParibasFortisConfiguration.getOpenbankStetVersion());
        persistentStorage.put(
                StorageKeys.API_BASE_URL, bnpParibasFortisConfiguration.getApiBaseUrl());
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
    protected Optional<EInvoiceRefreshController> constructEInvoiceRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<TransferDestinationRefreshController>
            constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new BnpParibasFortisSessionHandler(apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
