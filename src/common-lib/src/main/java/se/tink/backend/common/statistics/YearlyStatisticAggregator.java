package se.tink.backend.common.statistics;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.core.Statistic;
import se.tink.libraries.date.ResolutionTypes;

class YearlyStatisticAggregator {
    static final ImmutableSet<String> ENABLED_TYPES = ImmutableSet.of(
            Statistic.Types.EXPENSES_BY_CATEGORY,
            Statistic.Types.INCOME_AND_EXPENSES);

    static List<Statistic> transformMonthlyToYearly(List<Statistic> statistics) {
        return reduceToYearly(mapMonthlyStatistics(statistics));
    }

    private static List<Statistic> reduceToYearly(Map<String, Map<String, Map<String, Double>>> byPeriodTypeAndDescription) {
        return byPeriodTypeAndDescription.entrySet()
                .stream()
                .map(s -> {
                    Map<String, Map<String, Double>> periodDescription = s.getValue();
                    return periodDescription.entrySet().stream()
                            .map(pd -> {
                                Map<String, Double> descriptionValue = pd.getValue();
                                List<Statistic> statisticList = descriptionValue.entrySet().stream().map(desValue -> {
                                    Statistic statistic = new Statistic();
                                    statistic.setType(s.getKey());
                                    statistic.setDescription(desValue.getKey());
                                    statistic.setValue(desValue.getValue());
                                    statistic.setPeriod(pd.getKey());
                                    statistic.setResolution(ResolutionTypes.YEARLY);
                                    return statistic;
                                }).collect(Collectors.toList());
                                return statisticList;
                            });
                }).flatMap(collected -> collected.flatMap(Collection::stream)).collect(Collectors.toList());
    }

    private static Map<String, Map<String, Map<String, Double>>> mapMonthlyStatistics(List<Statistic> statistics) {
        return statistics.stream()
                .filter(s -> s.getResolution().equals(ResolutionTypes.MONTHLY) || s.getResolution().equals(ResolutionTypes.MONTHLY_ADJUSTED))
                .filter(s -> ENABLED_TYPES.contains(s.getType()))
                .map(s -> {
                    Statistic transformedStatistics = Statistic.copyOf(s);
                    String year = s.getPeriod().substring(0, 4);
                    transformedStatistics.setPeriod(year);
                    transformedStatistics.setResolution(ResolutionTypes.YEARLY);
                    return  transformedStatistics;
                }).collect(Collectors.groupingBy(Statistic::getType, Collectors.groupingBy(Statistic::getPeriod, Collectors.groupingBy(Statistic::getDescription, Collectors.summingDouble(Statistic::getValue)))));
    }
}
