package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard.entity.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard.entity.CreditCardTransactionsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@AllArgsConstructor
public class CreditCardTransactionFetcher implements TransactionFetcher<CreditCardAccount> {
    private final SdcNoApiClient bankClient;

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        CreditCardTransactionsEntity transactions =
                bankClient.fetchCreditCardTransactions(account.getApiIdentifier());

        return Stream.concat(
                        transactions.getBookedTransactions().stream(),
                        transactions.getPendingTransactions().stream())
                .map(CreditCardTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
