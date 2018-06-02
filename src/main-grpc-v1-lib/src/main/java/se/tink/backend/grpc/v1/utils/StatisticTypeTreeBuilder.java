package se.tink.backend.grpc.v1.utils;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.Map;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.UserProfile;
import se.tink.backend.grpc.v1.converter.statistic.StatisticNode;
import se.tink.backend.grpc.v1.converter.statistic.StatisticTypeTree;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class StatisticTypeTreeBuilder {
    private static final Maps.EntryTransformer<String, StatisticTreeBuilder, StatisticNode> MAP_STATISTIC_TREE_BUILDER_TO_NODE_TRANSFORMER =
            (s, statisticTreeBuilder) -> statisticTreeBuilder.build();
    private final UserProfile userProfile;
    private Map<String, Map<String, StatisticTreeBuilder>> statisticTypeTreeBuilder;

    public StatisticTypeTreeBuilder(UserProfile userProfile) {
        this.userProfile = userProfile;
        this.statisticTypeTreeBuilder = Maps.newHashMap();
    }

    public StatisticTypeTree build(Iterable<Statistic> statistics) {
        for (Statistic statistic : statistics) {
            addNode(statistic);
        }

        StatisticTypeTree statisticTypeTree = new StatisticTypeTree();

        for (String type : statisticTypeTreeBuilder.keySet()) {
            statisticTypeTree.put(type, Maps.transformEntries(statisticTypeTreeBuilder.get(type),
                    MAP_STATISTIC_TREE_BUILDER_TO_NODE_TRANSFORMER));
        }

        return statisticTypeTree;
    }

    private void addNode(Statistic statistic) {
        if (Strings.isNullOrEmpty(statistic.getDescription())) {
            statistic.setDescription(statistic.getType());
        }

        // Convert daily description into daily statistic (used in LEFT_TO_SPEND and LEFT_TO_SPEND_AVERAGE)
        if (ThreadSafeDateFormat.FORMATTER_DAILY.fitsFormat(statistic.getDescription())) {
            modifyToDailyStatistic(statistic);
        }

        String statisticType = statistic.getType();
        String statisticDescription = statistic.getDescription();

        Map<String, StatisticTreeBuilder> statisticDescriptionTreeBuilder = statisticTypeTreeBuilder.get(statisticType);
        if (statisticDescriptionTreeBuilder == null) {
            statisticDescriptionTreeBuilder = Maps.newHashMap();
            statisticTypeTreeBuilder.put(statisticType, statisticDescriptionTreeBuilder);
        }

        StatisticTreeBuilder statisticTreeBuilder = statisticDescriptionTreeBuilder.get(statisticDescription);
        if (statisticTreeBuilder == null) {
            statisticTreeBuilder = new StatisticTreeBuilder(userProfile);
            statisticDescriptionTreeBuilder.put(statisticDescription, statisticTreeBuilder);
        }

        statisticTreeBuilder.addNode(statistic);
    }

    private void modifyToDailyStatistic(Statistic statistic) {
        statistic.setResolution(ResolutionTypes.DAILY);
        statistic.setPeriod(statistic.getDescription());
        statistic.setDescription(statistic.getType());
    }
}
