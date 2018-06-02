package se.tink.backend.grpc.v1.converter.statistic;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.Map;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.periods.CorePeriodToGrpcPeriodConverter;
import se.tink.backend.grpc.v1.utils.NumberUtils;
import se.tink.grpc.v1.models.StatisticNode;

public class CoreStatisticNodeToGrpcStatisticNodeConverter {
    private final CorePeriodToGrpcPeriodConverter periodToGrpcConverter;

    CoreStatisticNodeToGrpcStatisticNodeConverter() {
        periodToGrpcConverter = new CorePeriodToGrpcPeriodConverter();
    }

    StatisticNode convertWithoutCurrency(se.tink.backend.grpc.v1.converter.statistic.StatisticNode node) {
        return convertFrom(node, null);
    }

    StatisticNode convertWithCurrency(se.tink.backend.grpc.v1.converter.statistic.StatisticNode node,
            String currency) {
        return convertFrom(node, currency);
    }

    private StatisticNode convertFrom(se.tink.backend.grpc.v1.converter.statistic.StatisticNode node, String currency) {
        StatisticNode.Builder builder = StatisticNode.newBuilder();

        // Put the node amount on the value if we don't have a currency
        if (Strings.isNullOrEmpty(currency)) {
            builder.setValue(NumberUtils.toExactNumber(node.getAmount()));
        } else {
            builder.setAmount(NumberUtils.toCurrencyDenominatedAmount(node.getAmount(), currency));
        }

        ConverterUtils.setIfPresent(node::getPeriod, builder::setPeriod, periodToGrpcConverter::convertFrom);
        builder.putAllChildren(convertChildren(node.getChildren(), currency));
        return builder.build();
    }

    private Map<String, StatisticNode> convertChildren(
            Map<String, se.tink.backend.grpc.v1.converter.statistic.StatisticNode> children, String currency) {
        Map<String, se.tink.grpc.v1.models.StatisticNode> statistics = Maps.newHashMapWithExpectedSize(children.size());
        for (String period : children.keySet()) {
            statistics.put(period, convertFrom(children.get(period), currency));
        }

        return statistics;
    }
}
