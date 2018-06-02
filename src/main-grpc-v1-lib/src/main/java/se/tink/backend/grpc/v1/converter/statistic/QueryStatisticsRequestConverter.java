package se.tink.backend.grpc.v1.converter.statistic;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.core.StatisticQuery;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.converter.periods.GrpcPeriodDescriptionToStringConverter;
import se.tink.grpc.v1.models.PeriodMode;
import se.tink.grpc.v1.rpc.QueryStatisticsRequest;
import se.tink.libraries.date.ResolutionTypes;

public class QueryStatisticsRequestConverter implements Converter<QueryStatisticsRequest, StatisticQuery> {
    private final GrpcPeriodDescriptionToStringConverter periodDescriptionToStringConverter = new GrpcPeriodDescriptionToStringConverter();
    private final ResolutionTypes userPeriodMode;

    public QueryStatisticsRequestConverter(ResolutionTypes userPeriodMode) {
        this.userPeriodMode = userPeriodMode;
    }

    @Override
    public StatisticQuery convertFrom(QueryStatisticsRequest input) {
        StatisticQuery query = new StatisticQuery();
        query.setDescription(Strings.isNullOrEmpty(input.getDescription()) ? null : input.getDescription());
        query.setPadResultUntilToday(input.getPadResultsUntilToday());

        if (input.getTypesCount() > 0) {
            List<String> statisticsType = input.getTypesList().stream()
                    .map(type -> EnumMappers.CORE_STATISTIC_TYPE_TO_GRPC_MAP.inverse().get(type))
                    .collect(Collectors.toList());
            query.setTypes(statisticsType);
        }

        if (Objects.equals(PeriodMode.PERIOD_MODE_MONTHLY, input.getPeriodMode())) {
            query.setResolution(userPeriodMode);
        } else {
            query.setResolution(EnumMappers.CORE_PERIOD_MODE_TO_GRPC_MAP.inverse().get(input.getPeriodMode()));
        }

        if (input.getPeriodsCount() > 0) {
            List<String> periods = input.getPeriodsList().stream()
                    .map(periodDescriptionToStringConverter::convertFrom)
                    .collect(Collectors.toList());
            query.setPeriods(periods);
        }

        return query;
    }
}
