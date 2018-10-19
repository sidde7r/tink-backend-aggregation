package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.UkOpenBankingAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.UkOpenBankingTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.UkOpenBankingUpcomingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.rpc.account.AccountBalanceV20Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.rpc.account.AccountsV20Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.rpc.transaction.AccountTransactionsV20Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.rpc.transaction.UpcomingTransactionsV20Response;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.common.config.SignatureKeyPair;

public class UkOpenBankingV20Agent extends UkOpenBankingAgent {

    public UkOpenBankingV20Agent(CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected UkOpenBankingAccountFetcher<?, ?, TransactionalAccount> makeTransactionalAccountFetcher(
            UkOpenBankingApiClient apiClient) {

        return new UkOpenBankingAccountFetcher<>(apiClient,
                AccountsV20Response.class,
                AccountBalanceV20Response.class,
                AccountsV20Response::toTransactionalAccount
        );
    }

    @Override
    protected TransactionKeyPaginationController<TransactionalAccount, ?> makeAccountTransactionPaginatorController(
            UkOpenBankingApiClient apiClient) {
        return new TransactionKeyPaginationController<>(
                new UkOpenBankingTransactionPaginator<>(
                        apiClient,
                        AccountTransactionsV20Response.class,
                        AccountTransactionsV20Response::toAccountTransactionPaginationResponse));
    }

    @Override
    protected Optional<UkOpenBankingUpcomingTransactionFetcher<?>> makeUpcomingTransactionFetcher(
            UkOpenBankingApiClient apiClient) {
        return Optional.empty();
        //TODO: Enable when this feature is mandatory for the banks to implement
//        return Optional.of(new UkOpenBankingUpcomingTransactionFetcher<>(apiClient,
//                UpcomingTransactionsV20Response.class,
//                UpcomingTransactionsV20Response::toUpcomingTransactions));
    }

    @Override
    protected UkOpenBankingAccountFetcher<?, ?, CreditCardAccount> makeCreditCardAccountFetcher(
            UkOpenBankingApiClient apiClient) {
        return new UkOpenBankingAccountFetcher<>(apiClient,
                AccountsV20Response.class,
                AccountBalanceV20Response.class,
                AccountsV20Response::toCreditCardAccount);
    }

    @Override
    protected TransactionKeyPaginationController<CreditCardAccount, ?> makeCreditCardTransactionPaginatorController(
            UkOpenBankingApiClient apiClient) {
        return new TransactionKeyPaginationController<>(
                new UkOpenBankingTransactionPaginator<>(
                        apiClient,
                        AccountTransactionsV20Response.class,
                        AccountTransactionsV20Response::toCreditCardPaginationResponse));
    }
}

