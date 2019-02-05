package se.tink.backend.aggregation.agents.nxgen.de.banks.santander;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.authenticator.SantanderPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.SantanderCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.SantanderAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.SantanderTransactionFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SantanderAgent extends NextGenerationAgent {

  private final SantanderApiClient santanderApiClient;

  public SantanderAgent(
      CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
    super(request, context, signatureKeyPair);
    santanderApiClient = new SantanderApiClient(this.client, sessionStorage);
  }

  @Override
  protected void configureHttpClient(TinkHttpClient client) {
  }

  @Override
  protected Authenticator constructAuthenticator() {
    return new PasswordAuthenticationController(
        new SantanderPasswordAuthenticator(santanderApiClient));
  }

  @Override
  protected Optional<TransactionalAccountRefreshController>
      constructTransactionalAccountRefreshController() {
    return Optional.of(
        new TransactionalAccountRefreshController(
            metricRefreshController,
            updateController,
            new SantanderAccountFetcher(santanderApiClient),
            new TransactionFetcherController<>(
                this.transactionPaginationHelper,
                new TransactionDatePaginationController<>(
                    new SantanderTransactionFetcher(santanderApiClient)))));
  }

  @Override
  protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {

    return Optional.of(
        new CreditCardRefreshController(
            metricRefreshController,
            updateController,
            new SantanderCreditCardFetcher(santanderApiClient),
            new TransactionFetcherController<>(
                transactionPaginationHelper,
                new TransactionKeyPaginationController<>(
                    new SantanderCreditCardFetcher(santanderApiClient)))));
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
    return new SantanderSessionHandler(santanderApiClient);
  }

  @Override
  protected Optional<TransferController> constructTransferController() {
    return Optional.empty();
  }
}
