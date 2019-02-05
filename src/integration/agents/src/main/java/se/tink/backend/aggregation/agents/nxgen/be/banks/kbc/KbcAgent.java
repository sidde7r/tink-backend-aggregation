package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.KbcAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor.KbcBankTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.KbcTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.KbcTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.filters.KbcHttpFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.kbc.fetchers.KbcCreditCardFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
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

import java.util.Locale;
import java.util.Optional;



public class KbcAgent extends NextGenerationAgent {
    private final KbcApiClient apiClient;
    private final String kbcLanguage;
    private KbcHttpFilter httpFilter;

    public KbcAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        kbcLanguage = getKbcLanguage(request.getUser().getLocale());
        this.apiClient = KbcApiClient.create(sessionStorage, client);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        httpFilter = new KbcHttpFilter();
        client.addFilter(httpFilter);
        client.setUserAgent(KbcConstants.Headers.USER_AGENT_VALUE);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        KbcAuthenticator authenticator = new KbcAuthenticator(
                persistentStorage,
                apiClient,
                supplementalInformationHelper);
        return new AutoAuthenticationController(request, context, authenticator, authenticator);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        KbcTransactionalAccountFetcher accountFetcher = new KbcTransactionalAccountFetcher(apiClient, kbcLanguage);
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(accountFetcher),
                                accountFetcher)));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        KbcCreditCardFetcher creditCardFetcher = new KbcCreditCardFetcher(apiClient);
        return Optional.of(
                new CreditCardRefreshController(metricRefreshController, updateController,
                        creditCardFetcher, creditCardFetcher));
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
    protected Optional<TransferDestinationRefreshController> constructTransferDestinationRefreshController() {
        return Optional.of(new TransferDestinationRefreshController(metricRefreshController,
                new KbcTransferDestinationFetcher(apiClient, kbcLanguage)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return KbcSessionHandler.create(httpFilter, apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.of(
                new TransferController(
                        null,
                        new KbcBankTransferExecutor(
                                credentials,
                                persistentStorage,
                                apiClient,
                                catalog,
                                supplementalInformationHelper),
                        null,
                        null));
    }

    private String getKbcLanguage(String locale) {
        if (Strings.isNullOrEmpty(locale)) {
            return Locale.ENGLISH.getLanguage();
        }
        if (locale.toLowerCase().contains(KbcConstants.LANGUAGE_DUTCH)) {
            return KbcConstants.LANGUAGE_DUTCH;
        }
        if (locale.toLowerCase().contains(Locale.FRENCH.getLanguage())) {
            return Locale.FRANCE.getLanguage();
        }
        if (locale.toLowerCase().contains(Locale.GERMAN.getLanguage())) {
            return Locale.GERMAN.getLanguage();
        }
        return Locale.ENGLISH.getLanguage();
    }

}
