package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.RevolutAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.RevolutMultifactorAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.RevolutTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.RevolutTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.filter.RevolutFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.session.RevolutSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticationPasswordController;
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
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class RevolutAgent extends NextGenerationAgent {
  private final RevolutApiClient apiClient;

  public RevolutAgent(
      CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
    super(request, context, signatureKeyPair);
    this.apiClient = new RevolutApiClient(client, persistentStorage);
  }

  @Override
  protected void configureHttpClient(TinkHttpClient client) {
    client.addFilter(new RevolutFilter());
  }

  @Override
  protected Authenticator constructAuthenticator() {
    SmsOtpAuthenticationPasswordController smsOtpAuthenticationController =
        new SmsOtpAuthenticationPasswordController<>(
            catalog,
            supplementalInformationHelper,
            new RevolutMultifactorAuthenticator(apiClient, persistentStorage),
            6);

    return new AutoAuthenticationController(
        request, context, smsOtpAuthenticationController, new RevolutAutoAuthenticator(apiClient));
  }

  @Override
  protected Optional<TransactionalAccountRefreshController>
      constructTransactionalAccountRefreshController() {
    return Optional.of(
        new TransactionalAccountRefreshController(
            metricRefreshController,
            updateController,
            new RevolutTransactionalAccountFetcher(apiClient),
            new TransactionFetcherController<>(
                transactionPaginationHelper,
                new TransactionKeyPaginationController<>(
                    new RevolutTransactionFetcher(apiClient)))));
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
    return new RevolutSessionHandler(apiClient);
  }

  @Override
  protected Optional<TransferController> constructTransferController() {
    return Optional.empty();
  }
}
