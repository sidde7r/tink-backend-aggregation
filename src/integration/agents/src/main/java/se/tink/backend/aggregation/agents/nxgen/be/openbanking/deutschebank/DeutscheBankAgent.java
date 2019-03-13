package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.authenticator.DeutscheBankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.configuration.DeutscheBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.DeutscheBankTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.session.DeutscheBankSessionHandler;
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
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class DeutscheBankAgent extends NextGenerationAgent {
  private final DeutscheBankApiClient apiClient;

  public DeutscheBankAgent(
      CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
    super(request, context, signatureKeyPair);

    apiClient = new DeutscheBankApiClient(client, sessionStorage, persistentStorage);
  }

  @Override
  public void setConfiguration(final AgentsServiceConfiguration configuration) {
    super.setConfiguration(configuration);

    final DeutscheBankConfiguration deutscheBankConfiguration =
        configuration
            .getIntegrations()
            .getClientConfiguration(
                DeutscheBankConstants.Market.INTEGRATION_NAME,
                DeutscheBankConstants.Market.CLIENT_NAME,
                DeutscheBankConfiguration.class)
            .orElseThrow(() -> new IllegalStateException("DeutscheBank configuration missing."));

    persistentStorage.put(
        DeutscheBankConstants.StorageKeys.BASE_URL, deutscheBankConfiguration.getBaseUrl());
    persistentStorage.put(
        DeutscheBankConstants.StorageKeys.CLIENT_ID, deutscheBankConfiguration.getClientId());
    persistentStorage.put(
        DeutscheBankConstants.StorageKeys.CLIENT_SECRET,
        deutscheBankConfiguration.getClientSecret());
    persistentStorage.put(
        DeutscheBankConstants.StorageKeys.REDIRECT_URI, deutscheBankConfiguration.getRedirectUri());
  }

  @Override
  protected void configureHttpClient(TinkHttpClient client) {}

  @Override
  protected Authenticator constructAuthenticator() {
    DeutscheBankAuthenticator authenticator = new DeutscheBankAuthenticator(apiClient);
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
    DeutscheBankTransactionalAccountFetcher accountFetcher =
        new DeutscheBankTransactionalAccountFetcher(apiClient);

    return Optional.of(
        new TransactionalAccountRefreshController(
            metricRefreshController,
            updateController,
            accountFetcher,
            new TransactionFetcherController<>(
                transactionPaginationHelper,
                new TransactionPagePaginationController<>(
                    accountFetcher, DeutscheBankConstants.Fetcher.START_PAGE))));
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
    return new DeutscheBankSessionHandler(apiClient, sessionStorage);
  }

  @Override
  protected Optional<TransferController> constructTransferController() {
    return Optional.empty();
  }
}
