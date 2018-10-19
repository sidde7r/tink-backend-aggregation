package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.UkOpenBankingAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.UkOpenBankingTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.UkOpenBankingUpcomingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.fetcher.rpc.account.AccountBalanceV11Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.fetcher.rpc.account.AccountsV11Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.fetcher.rpc.transaction.AccountTransactionsV11Response;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.common.config.SignatureKeyPair;

public class UkOpenBankingV11Agent extends UkOpenBankingAgent {

    public UkOpenBankingV11Agent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected UkOpenBankingAccountFetcher<?, ?, TransactionalAccount> makeTransactionalAccountFetcher(
            UkOpenBankingApiClient apiClient) {

        return new UkOpenBankingAccountFetcher<>(
                apiClient,
                AccountsV11Response.class,
                AccountBalanceV11Response.class,
                AccountsV11Response::toTransactionalAccount);
    }

    @Override
    protected TransactionKeyPaginationController<TransactionalAccount, ?> makeAccountTransactionPaginatorController(
            UkOpenBankingApiClient apiClient) {
        return new TransactionKeyPaginationController<>(
                new UkOpenBankingTransactionPaginator<>(
                        apiClient,
                        AccountTransactionsV11Response.class,
                        AccountTransactionsV11Response::toAccountTransactionPaginationResponse));
    }

    @Override
    protected Optional<UkOpenBankingUpcomingTransactionFetcher<?>> makeUpcomingTransactionFetcher(
            UkOpenBankingApiClient apiClient) {
        return Optional.empty(); // API v1.1 has no upcoming transactions
    }

    @Override
    protected UkOpenBankingAccountFetcher<?, ?, CreditCardAccount> makeCreditCardAccountFetcher(
            UkOpenBankingApiClient apiClient) {

        return new UkOpenBankingAccountFetcher<>(
                apiClient,
                AccountsV11Response.class,
                AccountBalanceV11Response.class,
                AccountsV11Response::toCreditCardAccount);
    }

    @Override
    protected TransactionKeyPaginationController<CreditCardAccount, ?> makeCreditCardTransactionPaginatorController(
            UkOpenBankingApiClient apiClient) {
        return new TransactionKeyPaginationController<>(
                new UkOpenBankingTransactionPaginator<>(
                        apiClient,
                        AccountTransactionsV11Response.class,
                        AccountTransactionsV11Response::toCreditCardPaginationResponse));
    }
}

