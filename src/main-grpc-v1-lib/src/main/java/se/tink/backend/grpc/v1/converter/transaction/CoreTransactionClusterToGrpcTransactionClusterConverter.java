package se.tink.backend.grpc.v1.converter.transaction;

import java.util.Map;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.utils.NumberUtils;
import se.tink.grpc.v1.models.TransactionCluster;

public class CoreTransactionClusterToGrpcTransactionClusterConverter
        implements Converter<se.tink.backend.core.TransactionCluster, TransactionCluster> {
    private final CoreTransactionToGrpcTransactionConverter transactionToGrpcConverter;

    public CoreTransactionClusterToGrpcTransactionClusterConverter(String currencyCode,
            Map<String, String> categoryCodeById) {
        transactionToGrpcConverter = new CoreTransactionToGrpcTransactionConverter(currencyCode, categoryCodeById);
    }

    @Override
    public TransactionCluster convertFrom(se.tink.backend.core.TransactionCluster input) {
        TransactionCluster.Builder builder = TransactionCluster.newBuilder();
        ConverterUtils.setIfPresent(input::getDescription, builder::setDescription);
        ConverterUtils.setIfPresent(input::getTransactions, builder::addAllTransactions,
                transactionToGrpcConverter::convertFrom);
        ConverterUtils.setIfPresent(input::getScore, builder::setScore, NumberUtils::toExactNumber);
        ConverterUtils.setIfPresent(input::getCategorizationImprovement, builder::setCategorizationImprovement,
                NumberUtils::toExactNumber);
        return builder.build();
    }
}
