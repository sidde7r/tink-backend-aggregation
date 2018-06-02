package se.tink.backend.grpc.v1.converter.follow;

import java.util.stream.Collectors;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.periods.CorePeriodToGrpcPeriodConverter;
import se.tink.backend.grpc.v1.utils.NumberUtils;
import se.tink.grpc.v1.models.FollowItemHistory;
import se.tink.grpc.v1.models.PeriodExactNumberPair;
import se.tink.libraries.date.Period;

public class CoreFollowItemHistoryToGrpcConverter
        implements Converter<se.tink.backend.core.follow.FollowItemHistory, FollowItemHistory> {
    private final CorePeriodToGrpcPeriodConverter periodConverter = new CorePeriodToGrpcPeriodConverter();

    @Override
    public FollowItemHistory convertFrom(se.tink.backend.core.follow.FollowItemHistory input) {
        FollowItemHistory.Builder builder = FollowItemHistory.newBuilder();

        builder.addAllHistoricalAmounts(input.getStatistics().entrySet()
                .stream().map(s -> convert(s.getKey(), s.getValue())).collect(Collectors.toList()));

        return builder.build();
    }

    private PeriodExactNumberPair convert(Period period, Double value) {
        return PeriodExactNumberPair.newBuilder()
                .setPeriod(periodConverter.convertFrom(period))
                .setValue(NumberUtils.toExactNumber(value))
                .build();
    }
}
