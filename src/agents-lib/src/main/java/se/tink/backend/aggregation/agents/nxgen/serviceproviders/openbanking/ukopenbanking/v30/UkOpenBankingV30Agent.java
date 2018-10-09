package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.UkOpenBankingAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.UkOpenBankingTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.v30.rpc.account.AccountBalanceV30Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.v30.rpc.account.AccountsV30Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.v30.rpc.transaction.AccountTransactionsV30Response;
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
    protected UkOpenBankingAccountFetcher<?, ?, TransactionalAccount> makeTransactionalAccountFetcher(UkOpenBankingApiClient apiClient) {
        return new UkOpenBankingAccountFetcher<>(apiClient,
                AccountsV30Response.class,
                AccountBalanceV30Response.class,
                AccountsV30Response::toTransactionalAccount);
    }

    @Override
    protected UkOpenBankingTransactionPaginator<?, TransactionalAccount> makeAccountTransactionPaginator(UkOpenBankingApiClient apiClient) {
        return new UkOpenBankingTransactionPaginator<>(apiClient,
                AccountTransactionsV30Response.class,
                AccountTransactionsV30Response::toAccountTransactionPaginationResponse);
    }

    @Override
    protected UkOpenBankingAccountFetcher<?, ?, CreditCardAccount> makeCreditCardAccountFetcher(
            UkOpenBankingApiClient apiClient) {
        return new UkOpenBankingAccountFetcher<>(apiClient,
                AccountsV30Response.class,
                AccountBalanceV30Response.class,
                AccountsV30Response::toCreditCardlAccount);
    }

    @Override
    protected UkOpenBankingTransactionPaginator<?, CreditCardAccount> makeCreditCardTransactionPaginator(
            UkOpenBankingApiClient apiClient) {
        return new UkOpenBankingTransactionPaginator<>(apiClient,
                AccountTransactionsV30Response.class,
                AccountTransactionsV30Response::toCreditCardPaginationResponse);
    }
}
