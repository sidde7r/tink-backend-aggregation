package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard.entity.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard.entity.CreditCardTransactionsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@AllArgsConstructor
public class CreditCardTransactionFetcher implements TransactionFetcher<CreditCardAccount> {
    private final SdcNoApiClient bankClient;

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        CreditCardTransactionsEntity transactions =
                bankClient.fetchCreditCardTransactions(account.getApiIdentifier());

        List<AggregationTransaction> bookedTransactions =
                transactions.getBookedTransactions().stream()
                        .map(transaction -> toTinkTransactions(transaction, false))
                        .collect(Collectors.toList());

        List<AggregationTransaction> pendingTransactions =
                transactions.getPendingTransactions().stream()
                        .map(transaction -> toTinkTransactions(transaction, true))
                        .collect(Collectors.toList());

        return Stream.of(bookedTransactions, pendingTransactions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private AggregationTransaction toTinkTransactions(
            CreditCardTransactionEntity transaction, boolean isPending) {
        return Transaction.builder()
                .setAmount(
                        ExactCurrencyAmount.of(
                                ObjectUtils.firstNonNull(
                                        transaction.getTransferedAmount(),
                                        transaction.getOriginalAmount()),
                                transaction.getCurrency()))
                .setDate(
                        ObjectUtils.firstNonNull(
                                transaction.getTransactionDate(),
                                transaction.getBookedTransactionDate()))
                .setDescription(transaction.getDescription())
                .setPending(isPending)
                .setType(TransactionTypes.CREDIT_CARD)
                .build();
    }
}
