package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.UkOpenBankingAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.UkOpenBankingTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.UkOpenBankingUpcomingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.rpc.account.AccountBalanceV30Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.rpc.account.AccountsV30Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.rpc.transaction.AccountTransactionsV30Response;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class UkOpenBankingV30Ais implements UkOpenBankingAis {
    @Override
    public UkOpenBankingAccountFetcher<?, ?, TransactionalAccount> makeTransactionalAccountFetcher(
            UkOpenBankingApiClient apiClient) {
        return new UkOpenBankingAccountFetcher<>(
                apiClient,
                AccountsV30Response.class,
                AccountBalanceV30Response.class,
                AccountsV30Response::toTransactionalAccount);
    }

    @Override
    public TransactionPaginator<TransactionalAccount> makeAccountTransactionPaginatorController(
            UkOpenBankingApiClient apiClient) {
        return new TransactionKeyPaginationController<>(
                new UkOpenBankingTransactionPaginator<>(
                        apiClient,
                        AccountTransactionsV30Response.class,
                        AccountTransactionsV30Response::toAccountTransactionPaginationResponse));
    }

    @Override
    public Optional<UkOpenBankingUpcomingTransactionFetcher<?>> makeUpcomingTransactionFetcher(
            UkOpenBankingApiClient apiClient) {
        return Optional.empty();
        // TODO: Enable when this feature is mandatory for the banks to implement
        //        return Optional.of(new UkOpenBankingUpcomingTransactionFetcher<>(apiClient,
        //                UpcomingTransactionsV30Response.class,
        //                UpcomingTransactionsV30Response::toUpcomingTransactions));
    }

    @Override
    public UkOpenBankingAccountFetcher<?, ?, CreditCardAccount> makeCreditCardAccountFetcher(
            UkOpenBankingApiClient apiClient) {
        return new UkOpenBankingAccountFetcher<>(
                apiClient,
                AccountsV30Response.class,
                AccountBalanceV30Response.class,
                AccountsV30Response::toCreditCardAccount);
    }

    @Override
    public TransactionPaginator<CreditCardAccount> makeCreditCardTransactionPaginatorController(
            UkOpenBankingApiClient apiClient) {
        return new TransactionKeyPaginationController<>(
                new UkOpenBankingTransactionPaginator<>(
                        apiClient,
                        AccountTransactionsV30Response.class,
                        AccountTransactionsV30Response::toCreditCardPaginationResponse));
    }
}
