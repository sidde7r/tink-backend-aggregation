package se.tink.backend.grpc.v1.converter.follow;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.transaction.CoreTransactionToGrpcTransactionConverter;
import se.tink.backend.grpc.v1.utils.NumberUtils;
import se.tink.grpc.v1.models.FollowData;
import se.tink.grpc.v1.models.Period;
import se.tink.grpc.v1.models.PeriodExactNumberPair;

public class CoreFollowDataToGrpcFollowDataConverter
        implements Converter<se.tink.backend.core.follow.FollowData, FollowData> {
    private final Map<String, Period> periodsByName;
    private final CoreTransactionToGrpcTransactionConverter transactionConverter;

    public CoreFollowDataToGrpcFollowDataConverter(Map<String, Period> periodsByName, String transactionCurrencyCode,
            Map<String, String> categoryCodeById) {
        this.periodsByName = periodsByName;
        transactionConverter = new CoreTransactionToGrpcTransactionConverter(transactionCurrencyCode, categoryCodeById);
    }

    @Override
    public FollowData convertFrom(se.tink.backend.core.follow.FollowData input) {
        FollowData.Builder builder = FollowData.newBuilder();
        List<PeriodExactNumberPair> historicalAmount = input.getHistoricalAmounts().stream()
                .filter(pair -> periodsByName.containsKey(pair.getKey()))
                .map(pair -> PeriodExactNumberPair.newBuilder().setPeriod(periodsByName.get(pair.getKey()))
                        .setValue(NumberUtils.toExactNumber(pair.getValue())).build())
                .collect(Collectors.toList());
        builder.addAllHistoricalAmounts(historicalAmount);
        ConverterUtils.setIfPresent(input::getPeriod, builder::setPeriod, periodsByName::get);
        ConverterUtils.setIfPresent(input::getPeriodTransactions, builder::addAllTransactions,
                transactionConverter::convertFrom);

        return builder.build();
    }
}
