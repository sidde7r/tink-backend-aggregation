package se.tink.backend.grpc.v1.converter.transaction;

import java.util.Map;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.utils.NumberUtils;
import se.tink.grpc.v1.rpc.SuggestTransactionsResponse;

public class SuggestTransactionsResponseConverter
        implements Converter<se.tink.backend.rpc.SuggestTransactionsResponse, SuggestTransactionsResponse> {
    private final CoreTransactionClusterToGrpcTransactionClusterConverter transactionClusterToGrpcConverter;

    public SuggestTransactionsResponseConverter(String currencyCode,
            Map<String, String> categoryCodeById) {
        transactionClusterToGrpcConverter = new CoreTransactionClusterToGrpcTransactionClusterConverter(currencyCode,
                categoryCodeById);
    }

    @Override
    public SuggestTransactionsResponse convertFrom(se.tink.backend.rpc.SuggestTransactionsResponse input) {
        SuggestTransactionsResponse.Builder builder = SuggestTransactionsResponse.newBuilder();
        ConverterUtils.setIfPresent(input::getCategorizationImprovement, builder::setCategorizationImprovement,
                NumberUtils::toExactNumber);
        ConverterUtils.setIfPresent(input::getCategorizationLevel, builder::setCategorizationLevel,
                NumberUtils::toExactNumber);
        ConverterUtils.setIfPresent(input::getClusters, builder::addAllClusters,
                transactionClusterToGrpcConverter::convertFrom);
        return builder.build();
    }
}
