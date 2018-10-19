package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.UkOpenBankingAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.UkOpenBankingTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.UkOpenBankingUpcomingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.rpc.account.AccountBalanceV30Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.rpc.account.AccountsV30Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.rpc.transaction.AccountTransactionsV30Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.rpc.transaction.UpcomingTransactionsV30Response;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.common.config.SignatureKeyPair;

public class UkOpenBankingV30Agent extends UkOpenBankingAgent {

    public UkOpenBankingV30Agent(CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected UkOpenBankingAccountFetcher<?, ?, TransactionalAccount> makeTransactionalAccountFetcher(
            UkOpenBankingApiClient apiClient) {
        return new UkOpenBankingAccountFetcher<>(apiClient,
                AccountsV30Response.class,
                AccountBalanceV30Response.class,
                AccountsV30Response::toTransactionalAccount);
    }

    @Override
    protected TransactionKeyPaginationController<TransactionalAccount, ?> makeAccountTransactionPaginatorController(
            UkOpenBankingApiClient apiClient) {
        return new TransactionKeyPaginationController<>(
                new UkOpenBankingTransactionPaginator<>(
                        apiClient,
                        AccountTransactionsV30Response.class,
                        AccountTransactionsV30Response::toAccountTransactionPaginationResponse));
    }

    @Override
    protected Optional<UkOpenBankingUpcomingTransactionFetcher<?>> makeUpcomingTransactionFetcher(
            UkOpenBankingApiClient apiClient) {
        return Optional.empty();
        //TODO: Enable when this feature is mandatory for the banks to implement
//        return Optional.of(new UkOpenBankingUpcomingTransactionFetcher<>(apiClient,
//                UpcomingTransactionsV30Response.class,
//                UpcomingTransactionsV30Response::toUpcomingTransactions));
    }

    @Override
    protected UkOpenBankingAccountFetcher<?, ?, CreditCardAccount> makeCreditCardAccountFetcher(
            UkOpenBankingApiClient apiClient) {
        return new UkOpenBankingAccountFetcher<>(apiClient,
                AccountsV30Response.class,
                AccountBalanceV30Response.class,
                AccountsV30Response::toCreditCardAccount);
    }

    @Override
    protected TransactionKeyPaginationController<CreditCardAccount, ?> makeCreditCardTransactionPaginatorController(
            UkOpenBankingApiClient apiClient) {
        return new TransactionKeyPaginationController<>(
                new UkOpenBankingTransactionPaginator<>(
                        apiClient,
                        AccountTransactionsV30Response.class,
                        AccountTransactionsV30Response::toCreditCardPaginationResponse));
    }
}
