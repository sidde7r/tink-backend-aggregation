package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.HandelsbankenBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.HandelsbankenSECardDeviceAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.ExecutorExceptionResolver;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.einvoice.HandelsbankenSEEInvoiceExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.payment.HandelsbankenSEPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.HandelsbankenSEBankTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.HandelsbankenSECreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.HandelsbankenSECreditCardTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.einvoice.HandelsbankenSEEInvoiceFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.HandelsbankenSEInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.HandelsbankenSEAccountTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.HandelsbankenSEUpcomingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transferdestination.HandelsbankenSETransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.HandelsbankenAutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index.TransactionIndexPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.utils.transfer.StringNormalizerSwedish;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.backend.aggregation.utils.transfer.TransferMessageLengthConfig;
import se.tink.backend.common.config.SignatureKeyPair;
import se.tink.libraries.i18n.Catalog;

public class HandelsbankenSEAgent
        extends HandelsbankenAgent<HandelsbankenSEApiClient, HandelsbankenSEConfiguration> {

    public HandelsbankenSEAgent(CredentialsRequest request, AgentContext context,
            SignatureKeyPair signatureKeyPair) {
        // useUniqueIdWithoutClearingNumber
        // temporary method to feature toggle what unique id to use for Handelsbanken SE
        // this support should be removed once all clusters have been migrated to use
        // Handelsbanken internal account number for transactional accounts and account
        // based credit cards (allkort)
        // *** the request costructor argument
        super(request, context, signatureKeyPair, new HandelsbankenSEConfiguration(request));
    }

    @Override
    protected HandelsbankenSEApiClient constructApiClient(
            HandelsbankenSEConfiguration handelsbankenConfiguration) {
        return new HandelsbankenSEApiClient(client, handelsbankenConfiguration);
    }

    @Override
    protected TypedAuthenticator[] constructAuthenticators(
            HandelsbankenSEApiClient bankClient,
            HandelsbankenSEConfiguration handelsbankenConfiguration,
            HandelsbankenPersistentStorage handelsbankenPersistentStorage,
            HandelsbankenSessionStorage handelsbankenSessionStorage) {

        HandelsbankenAutoAuthenticator autoAuthenticator = constructAutoAuthenticator();

        return new TypedAuthenticator[] {
                constructAutoAuthenticationController(
                        new HandelsbankenSECardDeviceAuthenticator(bankClient,
                                handelsbankenPersistentStorage,
                                new SupplementalInformationController(context, credentials),
                                handelsbankenConfiguration, autoAuthenticator),
                        autoAuthenticator),
                new BankIdAuthenticationController<>(context,
                        new HandelsbankenBankIdAuthenticator(bankClient,
                                credentials, handelsbankenPersistentStorage,
                                handelsbankenSessionStorage))
        };
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController(
            HandelsbankenSEApiClient bankClient,
            HandelsbankenSessionStorage handelsbankenSessionStorage) {
        return Optional
                .of(new InvestmentRefreshController(metricRefreshController, updateController,
                                new HandelsbankenSEInvestmentFetcher(bankClient,
                                        handelsbankenSessionStorage, credentials)
                        )
                );
    }

    @Override
    protected Optional<EInvoiceRefreshController> constructEInvoiceRefreshController(
            HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage) {
        return Optional.of(new EInvoiceRefreshController(
                metricRefreshController,
                updateController,
                new HandelsbankenSEEInvoiceFetcher(client, sessionStorage)
        ));
    }

    @Override
    protected Optional<TransferController> constructTranferController(
            HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage, AgentContext context) {

        HandelsbankenSEPaymentExecutor paymentExecutor = new HandelsbankenSEPaymentExecutor(client,
                sessionStorage);

        Catalog catalog = context.getCatalog();
        return Optional.of(new TransferController(
                paymentExecutor,
                new HandelsbankenSEBankTransferExecutor(
                        client,
                        sessionStorage,
                        new ExecutorExceptionResolver(catalog),
                        new TransferMessageFormatter(
                                catalog,
                                TransferMessageLengthConfig.createWithMaxLength(
                                        14,
                                        12),
                                new StringNormalizerSwedish(",.-?!/+"))),
                new HandelsbankenSEEInvoiceExecutor(client, sessionStorage),
                paymentExecutor));
    }

    @Override
    protected Optional<TransferDestinationRefreshController> constructTransferDestinationRefreshController(
            HandelsbankenSEApiClient client, HandelsbankenSessionStorage sessionStorage) {
        return Optional.of(new TransferDestinationRefreshController(metricRefreshController,
                updateController,
                new HandelsbankenSETransferDestinationFetcher(client, sessionStorage)));
    }

    @Override
    protected AccountFetcher<CreditCardAccount> constructCreditCardAccountFetcher(
            HandelsbankenSEApiClient client, HandelsbankenSessionStorage sessionStorage) {
        return new HandelsbankenSECreditCardAccountFetcher(client, sessionStorage);
    }

    @Override
    protected TransactionPaginator<TransactionalAccount> constructAccountTransactionPaginator(
            HandelsbankenSEApiClient client, HandelsbankenSessionStorage sessionStorage) {
        return new TransactionIndexPaginationController<>(
                new HandelsbankenSEAccountTransactionPaginator(client, sessionStorage));
    }

    @Override
    protected UpcomingTransactionFetcher<TransactionalAccount> constructUpcomingTransactionFetcher(
            HandelsbankenSEApiClient client, HandelsbankenSessionStorage sessionStorage) {
        return new HandelsbankenSEUpcomingTransactionFetcher(client, sessionStorage);
    }

    @Override
    protected TransactionPaginator<CreditCardAccount> constructCreditCardTransactionPaginator(
            HandelsbankenSEApiClient client, HandelsbankenSessionStorage sessionStorage) {
        return new TransactionKeyPaginationController<>(
                new HandelsbankenSECreditCardTransactionPaginator(client, sessionStorage)
        );
    }
}
