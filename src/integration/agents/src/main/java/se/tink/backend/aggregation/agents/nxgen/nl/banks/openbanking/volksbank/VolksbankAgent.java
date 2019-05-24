package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank;

import java.util.Base64;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.VolksbankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.configuration.VolksbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.VolksbankTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.VolksbankTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.session.VolksbankSessionHandler;
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
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class VolksbankAgent extends NextGenerationAgent {

    private final VolksbankApiClient volksbankApiClient;
    private final VolksbankHttpClient httpClient;
    private final VolksbankUtils utils;
    private final String BANK_PATH;
    private final String redirectUrl;

    public VolksbankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        BANK_PATH = request.getProvider().getPayload().split(" ")[1];
        redirectUrl = request.getProvider().getPayload().split(" ")[2];

        this.httpClient = new VolksbankHttpClient(client, "certificate");
        this.utils = new VolksbankUtils(BANK_PATH);

        volksbankApiClient = new VolksbankApiClient(httpClient, sessionStorage, utils);
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final VolksbankConfiguration volksbankConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                VolksbankConstants.Market.INTEGRATION_NAME,
                                VolksbankConstants.Market.CLIENT_NAME,
                                VolksbankConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Volksbank configuration missing."));

        volksbankApiClient.setConfiguration(volksbankConfiguration);

        httpClient.setSslClientCertificate(
                Base64.getDecoder()
                        .decode(
                                volksbankConfiguration
                                        .getAisConfiguration()
                                        .getClientCertificateContent()),
                volksbankConfiguration.getAisConfiguration().getClientCertificatePass());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        VolksbankAuthenticator authenticator =
                new VolksbankAuthenticator(volksbankApiClient, sessionStorage, redirectUrl, utils);

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
                        new VolksbankTransactionalAccountFetcher(volksbankApiClient),
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(
                                        new VolksbankTransactionFetcher(volksbankApiClient)))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
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
        return new VolksbankSessionHandler();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
