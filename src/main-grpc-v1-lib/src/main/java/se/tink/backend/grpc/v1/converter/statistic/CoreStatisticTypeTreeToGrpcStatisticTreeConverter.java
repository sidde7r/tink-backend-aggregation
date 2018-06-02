package se.tink.backend.grpc.v1.converter.statistic;

import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.core.Statistic;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.grpc.v1.models.StatisticTree;

public class CoreStatisticTypeTreeToGrpcStatisticTreeConverter implements Converter<StatisticTypeTree, StatisticTree> {
    private final CoreStatisticNodeToGrpcStatisticNodeConverter statisticNodeToGrpcConverter;
    private final Map<String, String> categoryCodeById;
    private final String currencyCode;

    public CoreStatisticTypeTreeToGrpcStatisticTreeConverter(String currencyCode,
            Map<String, String> categoryCodeById) {
        statisticNodeToGrpcConverter = new CoreStatisticNodeToGrpcStatisticNodeConverter();
        this.categoryCodeById = categoryCodeById;
        this.currencyCode = currencyCode;
    }

    @Override
    public StatisticTree convertFrom(StatisticTypeTree input) {
        StatisticTree.Builder builder = StatisticTree.newBuilder();

        // These statistics are connected to currency
        builder.putAllBalancesByAccountId(map(input.get(Statistic.Types.BALANCES_BY_ACCOUNT)));
        builder.putAllBalancesByAccountGroupType(map(input.get(Statistic.Types.BALANCES_BY_ACCOUNT_TYPE_GROUP)));
        builder.putAllLoanBalancesByProperty(map(input.get(Statistic.Types.LOAN_BALANCES_BY_PROPERTY)));
        builder.putAllLeftToSpend(map(getLeftToSpendStatistics(input)));
        builder.putAllExpensesByCategoryCode(map(input.get(Statistic.Types.EXPENSES_BY_CATEGORY), categoryCodeById::get,
                n -> statisticNodeToGrpcConverter.convertWithCurrency(n, currencyCode)));
        builder.putAllIncomeByCategoryCode(map(input.get(Statistic.Types.INCOME_BY_CATEGORY), categoryCodeById::get,
                n -> statisticNodeToGrpcConverter.convertWithCurrency(n, currencyCode)));

        // These statistics are not connected to a currency
        builder.putAllLoanRatesByProperty(map(input.get(Statistic.Types.LOAN_RATES_BY_PROPERTY), Function.identity(),
                statisticNodeToGrpcConverter::convertWithoutCurrency));

        return builder.build();
    }

    private Map<String, StatisticNode> getLeftToSpendStatistics(StatisticTypeTree statisticTypeTree) {
        Optional<StatisticNode> leftToSpend = getAnyElement(statisticTypeTree, Statistic.Types.LEFT_TO_SPEND);
        Optional<StatisticNode> leftToSpendAvg = getAnyElement(statisticTypeTree,
                Statistic.Types.LEFT_TO_SPEND_AVERAGE);

        Map<String, StatisticNode> leftToSpendDescriptionMap = Maps.newHashMap();
        leftToSpend.ifPresent(statisticNode -> leftToSpendDescriptionMap.put("actual", statisticNode));
        leftToSpendAvg.ifPresent(statisticNode -> leftToSpendDescriptionMap.put("average", statisticNode));

        return leftToSpendDescriptionMap;
    }

    private Optional<StatisticNode> getAnyElement(StatisticTypeTree statisticTypeTree, String key) {
        if (statisticTypeTree.containsKey(key) && !statisticTypeTree.get(key).isEmpty()) {
            return statisticTypeTree.get(key).values().stream().findAny();
        }
        return Optional.empty();
    }

    private Map<String, se.tink.grpc.v1.models.StatisticNode> map(Map<String, StatisticNode> statistics) {
        return map(statistics, Function.identity(),
                n -> statisticNodeToGrpcConverter.convertWithCurrency(n, currencyCode));
    }

    private <K1, V1, K2, V2> Map<K2, V2> map(Map<K1, V1> map, Function<K1, K2> keyFunction,
            Function<V1, V2> valueFunction) {
        return Optional.ofNullable(map).orElse(Collections.emptyMap())
                .entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> keyFunction.apply(entry.getKey()),
                        entry -> valueFunction.apply(entry.getValue())));
    }
}
