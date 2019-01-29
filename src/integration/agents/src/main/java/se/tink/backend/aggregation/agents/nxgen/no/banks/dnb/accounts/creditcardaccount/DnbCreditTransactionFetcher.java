package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.rpc.FetchCreditCardTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class DnbCreditTransactionFetcher implements TransactionFetcher<CreditCardAccount> {

    private DnbApiClient apiClient;

    public DnbCreditTransactionFetcher(DnbApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        FetchCreditCardTransactionsResponse fetchResponse = apiClient.fetchCreditCardTransactions(account);

        return fetchResponse.getTransactions().stream()
                .map(TransactionsEntity::getTransactions)
                .flatMap(Collection::stream)
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
