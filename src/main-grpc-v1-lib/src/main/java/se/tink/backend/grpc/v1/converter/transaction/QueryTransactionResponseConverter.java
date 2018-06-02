package se.tink.backend.grpc.v1.converter.transaction;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.core.SearchResult;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.utils.NumberUtils;
import se.tink.backend.rpc.SearchResponse;
import se.tink.grpc.v1.models.Transaction;
import se.tink.grpc.v1.rpc.QueryTransactionsResponse;

public class QueryTransactionResponseConverter implements Converter<SearchResponse, QueryTransactionsResponse> {
    private final CoreTransactionToGrpcTransactionConverter transactionToGrpcConverter;
    private final String currencyCode;

    public QueryTransactionResponseConverter(String currencyCode, Map<String, String> categoryCodeById) {
        this.transactionToGrpcConverter = new CoreTransactionToGrpcTransactionConverter(currencyCode, categoryCodeById);
        this.currencyCode = currencyCode;
    }

    @Override
    public QueryTransactionsResponse convertFrom(SearchResponse input) {
        QueryTransactionsResponse.Builder builder = QueryTransactionsResponse.newBuilder();

        List<Transaction> transactions = input.getResults()
                .stream()
                .map(SearchResult::getTransaction)
                .map(transactionToGrpcConverter::convertFrom)
                .collect(Collectors.toList());

        builder.addAllTransactions(transactions);
        builder.setHasMore(input.getCount() > input.getResults().size());
        builder.setTotalCount(input.getCount());
        builder.setTotalNetAmount(NumberUtils.toCurrencyDenominatedAmount(input.getNet(), currencyCode));

        return builder.build();
    }
}
