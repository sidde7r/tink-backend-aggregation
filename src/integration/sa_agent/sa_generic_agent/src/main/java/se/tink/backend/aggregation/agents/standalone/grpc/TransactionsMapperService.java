package se.tink.backend.aggregation.agents.standalone.grpc;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.sa.services.fetch.trans.TransactionsMapEntity;

public class TransactionsMapperService {

    public static se.tink.sa.services.fetch.trans.FetchTransactionsRequest
            mapFetchTransactionsRequest() {
        se.tink.sa.services.fetch.trans.FetchTransactionsRequest.Builder request =
                se.tink.sa.services.fetch.trans.FetchTransactionsRequest.newBuilder();
        // TODO
        return request.build();
    }

    public static FetchTransactionsResponse mapFetchTransactionsResponse(
            final se.tink.sa.services.fetch.trans.FetchTransactionsResponse
                    fetchTransactionsResponse) {
        return new FetchTransactionsResponse(
                mapTransactions(fetchTransactionsResponse.getTransactionsList()));
    }

    private static Map<Account, List<Transaction>> mapTransactions(
            final List<TransactionsMapEntity> transactionsMapEntityList) {
        return Optional.ofNullable(transactionsMapEntityList).orElse(Collections.emptyList())
                .stream()
                .collect(
                        Collectors.toMap(
                                transactionsMapEntity ->
                                        AccountMapperService.mapAccount(
                                                transactionsMapEntity.getKey()),
                                transactionsMapEntity ->
                                        TransactionsMapperService.mapTransaction(
                                                transactionsMapEntity.getValueList())));
    }

    private static List<Transaction> mapTransaction(
            final List<se.tink.sa.services.fetch.trans.Transaction> transaction) {
        return null;
    }
}
