package se.tink.backend.grpc.v1.converter.statistic;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.UserProfile;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.grpc.v1.models.StatisticTree;

public class FirehoseStatisticsToGrpcStatisticTreeConverter
        implements Converter<Iterable<se.tink.backend.firehose.v1.models.Statistic>, StatisticTree> {
    private final CoreStatisticsToGrpcStatisticTreeConverter statisticsToGrpcConverter;

    public FirehoseStatisticsToGrpcStatisticTreeConverter(UserProfile userProfile,
            Map<String, String> categoryCodeById) {
        statisticsToGrpcConverter = new CoreStatisticsToGrpcStatisticTreeConverter(userProfile, categoryCodeById);
    }

    @Override
    public StatisticTree convertFrom(Iterable<se.tink.backend.firehose.v1.models.Statistic> statistics) {
        List<Statistic> coreStatistics = StreamSupport.stream(statistics.spliterator(), false)
                .map(this::convertFrom)
                .collect(Collectors.toList());

        return statisticsToGrpcConverter.convertFrom(coreStatistics);
    }

    private Statistic convertFrom(se.tink.backend.firehose.v1.models.Statistic statistic) {
        Statistic coreStatistic = new Statistic();
        ConverterUtils.setIfPresent(statistic::getDescription, coreStatistic::setDescription);
        ConverterUtils.setIfPresent(statistic::getPayload, coreStatistic::setPayload);
        ConverterUtils.setIfPresent(statistic::getPeriod, coreStatistic::setPeriod);
        ConverterUtils.setIfPresent(statistic::getResolution, coreStatistic::setResolution,
                resolution -> EnumMappers.FIREHOSE_RESOLUTION_TYPE_TO_CORE_MAP
                        .getOrDefault(resolution, ResolutionTypes.ALL));
        ConverterUtils.setIfPresent(statistic::getType, coreStatistic::setType);
        ConverterUtils.setIfPresent(statistic::getUserId, coreStatistic::setUserId);
        ConverterUtils.setIfPresent(statistic::getValue, coreStatistic::setValue);

        return coreStatistic;
    }
}
