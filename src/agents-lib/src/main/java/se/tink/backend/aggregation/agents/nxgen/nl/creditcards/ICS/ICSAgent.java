package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.ICSOAuthAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.ICSAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.ICSCreditCardFetcher;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.ICSConfiguration;
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
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class ICSAgent extends NextGenerationAgent {

  private final ICSApiClient icsApiClient;

  public ICSAgent(
      CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
    super(request, context, signatureKeyPair);
    icsApiClient = new ICSApiClient(client, sessionStorage, persistentStorage);
  }

  @Override
  protected void configureHttpClient(TinkHttpClient client) {
    client.disableSignatureRequestHeader();
  }

  @Override
  public void setConfiguration(AgentsServiceConfiguration configuration) {
    super.setConfiguration(configuration);
    ICSConfiguration icsConfiguration = configuration.getIntegrations().getIcsConfiguration();

    if (icsConfiguration == null || icsConfiguration.isEmpty()) {
      throw new IllegalStateException("ICS Configuration is empty!");
    }

    client.setSslClientCertificate(
        EncodingUtils.decodeBase64String(icsConfiguration.getClientSSLCertificate()), "");
    client.trustRootCaCertificate(
        EncodingUtils.decodeBase64String(icsConfiguration.getRootCACertificate()),
        icsConfiguration.getRootCAPassword());

    persistentStorage.put(ICSConstants.Storage.ICS_CONFIGURATION, icsConfiguration);
  }

  @Override
  protected Authenticator constructAuthenticator() {
    ICSOAuthAuthenticator authenticator = new ICSOAuthAuthenticator(icsApiClient);
    OAuth2AuthenticationController oAuth2AuthenticationController =
        new OAuth2AuthenticationController(
            persistentStorage, supplementalInformationController, authenticator);
    return new AutoAuthenticationController(
        request,
        context,
        new ThirdPartyAppAuthenticationController<>(
            oAuth2AuthenticationController, supplementalInformationController),
        oAuth2AuthenticationController);
  }

  @Override
  protected Optional<TransactionalAccountRefreshController>
      constructTransactionalAccountRefreshController() {
    return Optional.empty();
  }

  @Override
  protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
    return Optional.of(
        new CreditCardRefreshController(
            metricRefreshController,
            updateController,
            new ICSAccountFetcher(icsApiClient),
            new TransactionFetcherController<>(
                transactionPaginationHelper,
                new TransactionPagePaginationController<>(
                    new ICSCreditCardFetcher(icsApiClient), 0),
                null)));
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
    return new ICSSessionHandler();
  }

  @Override
  protected Optional<TransferController> constructTransferController() {
    return Optional.empty();
  }
}
