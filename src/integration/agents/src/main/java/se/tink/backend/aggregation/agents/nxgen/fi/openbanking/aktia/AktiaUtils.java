package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;

@UtilityClass
public class AktiaUtils {

    public FetchTransactionsResponse removeDuplicates(
            FetchTransactionsResponse fetchTransactionsResponse) {

        List<Account> accounts = new ArrayList<>();
        fetchTransactionsResponse.getTransactions().forEach((a, t) -> accounts.add(a));

        for (int i = 0; i < fetchTransactionsResponse.getTransactions().size(); i++) {

            final Account number = accounts.get(i);

            List<Transaction> transactionsToReplace =
                    fetchTransactionsResponse.getTransactions().get(number);

            if (!transactionsToReplace.stream().findFirst().isPresent())
                return fetchTransactionsResponse;
            else {
                List<Transaction> resultTrx =
                        transactionsToReplace.stream()
                                .filter(
                                        distinctByKey(
                                                transaction ->
                                                        transaction
                                                                .getExternalSystemIds()
                                                                .get(
                                                                        TransactionExternalSystemIdType
                                                                                .PROVIDER_GIVEN_TRANSACTION_ID)))
                                .collect(Collectors.toList());

                transactionsToReplace.clear();
                transactionsToReplace.addAll(resultTrx);
            }
        }
        return fetchTransactionsResponse;
    }

    private <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
