package se.tink.backend.grpc.v1.converter.transaction;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import java.util.Map;
import se.tink.backend.core.SearchQuery;
import se.tink.backend.core.SearchSortTypes;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.grpc.v1.rpc.QueryTransactionsRequest;

public class QueryTransactionRequestConverter implements Converter<QueryTransactionsRequest, SearchQuery> {
    private final Map<String, String> categoryIdByCode;

    public QueryTransactionRequestConverter(Map<String, String> categoryIdByCode) {
        this.categoryIdByCode = categoryIdByCode;
    }

    @Override
    public SearchQuery convertFrom(QueryTransactionsRequest input) {
        SearchQuery searchQuery = new SearchQuery();
        ConverterUtils.setIfPresent(input::hasLimit, input::getLimit, searchQuery::setLimit, Int32Value::getValue);
        ConverterUtils
                .setIfPresent(input::hasLastTransactionId, input::getLastTransactionId,
                        searchQuery::setLastTransactionId, StringValue::getValue);
        ConverterUtils.setIfPresent(input::getAccountIdsList, searchQuery::setAccounts);
        ConverterUtils.setIfPresent(input::getCredentialIdsList, searchQuery::setCredentials);
        ConverterUtils.mapList(input::getCategoryCodesList, searchQuery::setCategories, categoryIdByCode::get);
        ConverterUtils.setIfPresent(input::hasStartDate, input::getStartDate, searchQuery::setStartDate,
                ProtobufModelUtils::timestampToDate);
        ConverterUtils.setIfPresent(input::hasEndDate, input::getEndDate, searchQuery::setEndDate,
                ProtobufModelUtils::timestampToDate);
        ConverterUtils
                .setIfPresent(input::hasIncludeUpcoming, input::getIncludeUpcoming, searchQuery::setIncludeUpcoming,
                        BoolValue::getValue);
        ConverterUtils.setIfPresent(input::hasQueryString, input::getQueryString, searchQuery::setQueryString,
                StringValue::getValue);
        searchQuery.setSort(SearchSortTypes.DATE);

        return searchQuery;
    }
}
