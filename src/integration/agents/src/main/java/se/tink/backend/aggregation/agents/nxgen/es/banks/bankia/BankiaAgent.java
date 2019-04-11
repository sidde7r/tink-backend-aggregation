package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia;

import java.util.Base64;
import java.util.Optional;
import java.util.Random;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.authenticator.BankiaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.creditcard.BankiaCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.BankiaInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.BankiaLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.BankiaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.session.BankiaSessionHandler;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
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
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class BankiaAgent extends NextGenerationAgent {

    private final BankiaApiClient apiClient;

    public BankiaAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new BankiaApiClient(client, persistentStorage);

        checkDeviceId();
    }

    private void checkDeviceId() {
        String base64 = persistentStorage.get(BankiaConstants.StorageKey.DEVICE_ID_BASE_64);
        String safe = persistentStorage.get(BankiaConstants.StorageKey.DEVICE_ID_BASE_64_URL);

        if (base64 == null || safe == null) {
            byte[] bytes = new byte[128];
            new Random().nextBytes(bytes);

            String base64Encoded = EncodingUtils.encodeAsBase64String(bytes);
            String base64EncodedSafe =
                    Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

            persistentStorage.put(BankiaConstants.StorageKey.DEVICE_ID_BASE_64, base64Encoded);
            persistentStorage.put(
                    BankiaConstants.StorageKey.DEVICE_ID_BASE_64_URL, base64EncodedSafe);
        }
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {}

    @Override
    protected Authenticator constructAuthenticator() {
        BankiaAuthenticator authenticator = new BankiaAuthenticator(apiClient);
        return new PasswordAuthenticationController(authenticator);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        BankiaTransactionalAccountFetcher fetcher =
                new BankiaTransactionalAccountFetcher(apiClient);
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        fetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionDatePaginationController<>(fetcher))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        BankiaCreditCardFetcher creditCardFetcher = new BankiaCreditCardFetcher(apiClient);
        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        creditCardFetcher,
                        creditCardFetcher));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        BankiaInvestmentFetcher fetcher = new BankiaInvestmentFetcher(apiClient);
        return Optional.of(
                new InvestmentRefreshController(
                        metricRefreshController, updateController, fetcher));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new BankiaLoanFetcher(apiClient)));
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
        return new BankiaSessionHandler(apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
