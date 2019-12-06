package se.tink.backend.aggregation.agents.standalone.mapper.fetch.trans.agg;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg.TransactionAccountMapper;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.trans.TransactionsMapEntity;

public class FetchTransactionsResponseMapper
        implements Mapper<
                FetchTransactionsResponse,
                se.tink.sa.services.fetch.trans.FetchTransactionsResponse> {

    private TransactionAccountMapper transactionAccountMapper;

    private TransactionMapper transactionMapper;

    public void setTransactionAccountMapper(TransactionAccountMapper transactionAccountMapper) {
        this.transactionAccountMapper = transactionAccountMapper;
    }

    public void setTransactionMapper(TransactionMapper transactionMapper) {
        this.transactionMapper = transactionMapper;
    }

    @Override
    public FetchTransactionsResponse map(
            se.tink.sa.services.fetch.trans.FetchTransactionsResponse source,
            MappingContext mappingContext) {
        return new FetchTransactionsResponse(
                mapTransactions(source.getTransactionsList(), mappingContext));
    }

    private Map<Account, List<Transaction>> mapTransactions(
            final List<TransactionsMapEntity> transactionsMapEntityList,
            MappingContext mappingContext) {

        return Optional.ofNullable(transactionsMapEntityList).orElse(Collections.emptyList())
                .stream()
                .collect(
                        Collectors.toMap(
                                transactionsMapEntity ->
                                        transactionAccountMapper.map(
                                                transactionsMapEntity.getKey()),
                                transactionsMapEntity ->
                                        mapTransactionList(
                                                transactionsMapEntity.getValueList(),
                                                mappingContext)));
    }

    private List<Transaction> mapTransactionList(
            final List<se.tink.sa.services.fetch.trans.Transaction> transaction,
            MappingContext mappingContext) {
        return Optional.ofNullable(transaction).orElse(Collections.emptyList()).stream()
                .map(t -> transactionMapper.map(t, mappingContext))
                .collect(Collectors.toList());
    }
}
