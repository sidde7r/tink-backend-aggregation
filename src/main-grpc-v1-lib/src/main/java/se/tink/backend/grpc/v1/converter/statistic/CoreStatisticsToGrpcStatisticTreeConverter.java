package se.tink.backend.grpc.v1.converter.statistic;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;

import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.UserProfile;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.utils.StatisticTreeBuilder;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.grpc.v1.models.StatisticTree;

public class CoreStatisticsToGrpcStatisticTreeConverter implements Converter<Iterable<Statistic>, StatisticTree> {
    private final CoreStatisticTypeTreeToGrpcStatisticTreeConverter statisticTypeTreeToGrpcConverter;
    private final UserProfile userProfile;

    public CoreStatisticsToGrpcStatisticTreeConverter(UserProfile userProfile,
            Map<String, String> categoryCodeById) {
        this.userProfile = userProfile;
        this.statisticTypeTreeToGrpcConverter = new CoreStatisticTypeTreeToGrpcStatisticTreeConverter(
                userProfile.getCurrency(), categoryCodeById);
    }

    @Override
    public StatisticTree convertFrom(Iterable<Statistic> statistics) {
        Map<String, Map<String, StatisticTreeBuilder>> statisticTypeTreeBuilder = Maps.newHashMap();

        for (Statistic statistic : statistics) {
            if (Objects.equals(statistic.getResolution(), ResolutionTypes.YEARLY)) {
                continue;
            }
            addNode(statisticTypeTreeBuilder, statistic);
        }

        StatisticTypeTree statisticTypeTree = new StatisticTypeTree();

        for (String type : statisticTypeTreeBuilder.keySet()) {
            statisticTypeTree.put(type, Maps.transformEntries(statisticTypeTreeBuilder.get(type),
                    (s, statisticTreeBuilder) -> statisticTreeBuilder.build()));
        }

        return statisticTypeTreeToGrpcConverter.convertFrom(statisticTypeTree);
    }

    private void addNode(Map<String, Map<String, StatisticTreeBuilder>> statisticTypeTreeBuilder, Statistic statistic) {
        if (Strings.isNullOrEmpty(statistic.getDescription())) {
            statistic.setDescription(statistic.getType());
        }

        // Convert daily description into daily statistic (used in LEFT_TO_SPEND and LEFT_TO_SPEND_AVERAGE)
        if (ThreadSafeDateFormat.FORMATTER_DAILY.fitsFormat(statistic.getDescription())) {
            modifyToDailyStatistic(statistic);
        }

        String statisticType = statistic.getType();
        String statisticDescription = statistic.getDescription();

        Map<String, StatisticTreeBuilder> statisticDescriptionTreeBuilder = statisticTypeTreeBuilder
                .computeIfAbsent(statisticType, k -> Maps.newHashMap());

        StatisticTreeBuilder statisticTreeBuilder = statisticDescriptionTreeBuilder
                .computeIfAbsent(statisticDescription, k -> new StatisticTreeBuilder(userProfile));

        statisticTreeBuilder.addNode(statistic);
    }

    private void modifyToDailyStatistic(Statistic statistic) {
        statistic.setResolution(ResolutionTypes.DAILY);
        statistic.setPeriod(statistic.getDescription());
        statistic.setDescription(statistic.getType());
    }
}
