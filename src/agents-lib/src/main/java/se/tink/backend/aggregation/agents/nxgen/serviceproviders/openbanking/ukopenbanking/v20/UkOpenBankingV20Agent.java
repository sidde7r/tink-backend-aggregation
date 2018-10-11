package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.UkOpenBankingAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.UkOpenBankingTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.rpc.account.AccountBalanceV20Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.rpc.account.AccountsV20Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.rpc.transaction.AccountTransactionsV20Response;
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
    protected UkOpenBankingTransactionPaginator<?, TransactionalAccount> makeAccountTransactionPaginator(
            UkOpenBankingApiClient apiClient) {

        return new UkOpenBankingTransactionPaginator<>(apiClient,
                AccountTransactionsV20Response.class,
                AccountTransactionsV20Response::toAccountTransactionPaginationResponse);
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
    protected UkOpenBankingTransactionPaginator<?, CreditCardAccount> makeCreditCardTransactionPaginator(
            UkOpenBankingApiClient apiClient) {
        return new UkOpenBankingTransactionPaginator<>(apiClient,
                AccountTransactionsV20Response.class,
                AccountTransactionsV20Response::toCreditCardPaginationResponse);
    }
}

