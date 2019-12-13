package se.tink.backend.aggregation.agents.standalone.mapper.fetch.trans.agg;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;

public class FetchTransactionsResponseMapper
        implements Mapper<
                TransactionKeyPaginatorResponseImpl,
                se.tink.sa.services.fetch.trans.FetchTransactionsResponse> {

    private TransactionEntityMapper transactionEntityMapper;

    private TransactionLinksEntityMapper transactionLinksEntityMapper;

    public void setTransactionEntityMapper(TransactionEntityMapper transactionEntityMapper) {
        this.transactionEntityMapper = transactionEntityMapper;
    }

    public void setTransactionLinksEntityMapper(
            TransactionLinksEntityMapper transactionLinksEntityMapper) {
        this.transactionLinksEntityMapper = transactionLinksEntityMapper;
    }

    @Override
    public TransactionKeyPaginatorResponseImpl map(
            se.tink.sa.services.fetch.trans.FetchTransactionsResponse source,
            MappingContext mappingContext) {

        Collection<? extends Transaction> transactions =
                Optional.ofNullable(source.getTransactionsEntityList())
                        .orElse(Collections.emptyList()).stream()
                        .map(
                                transactionEntity ->
                                        transactionEntityMapper.map(
                                                transactionEntity, mappingContext))
                        .collect(Collectors.toList());

        TransactionKeyPaginatorResponseImpl resp =
                new TransactionKeyPaginatorResponseImpl(
                        transactions,
                        transactionLinksEntityMapper.map(
                                source.getTransactionLinksEntity(), mappingContext));
        return resp;
    }
}
