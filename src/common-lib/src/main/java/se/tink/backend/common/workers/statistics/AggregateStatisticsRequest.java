package se.tink.backend.common.workers.statistics;

import java.util.Collection;
import java.util.List;

import java.util.function.Function;
import se.tink.backend.common.statistics.factory.TransactionStatisticTransformerFactory;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;

import com.google.common.collect.Lists;

public class AggregateStatisticsRequest {

    public static class GroupReducePair {
        public Function<Statistic, Integer> grouper;
        public Function<Collection<Statistic>, Collection<Statistic>> reducer;

        public GroupReducePair(Function<Statistic, Integer> grouper,
                Function<Collection<Statistic>, Collection<Statistic>> reducer) {
            this.grouper = grouper;
            this.reducer = reducer;
        }
    }

    private String type;
    private Function<Transaction, Statistic> transformationFunction;

    private List<GroupReducePair> groupReducePairs;
    private boolean disregardAccountOwnership;
    private TransactionStatisticTransformerFactory transformerFactory;

    public AggregateStatisticsRequest(String type) {
        this.type = type;
        groupReducePairs = Lists.newArrayList();
    }

    public String getType() {
        return type;
    }

    public Function<Transaction, Statistic> getTransformationFunction() {
        return transformationFunction;
    }

    public List<GroupReducePair> getGroupReducePairs() {
        return groupReducePairs;
    }

    public boolean isDisregardAccountOwnership() {
        return disregardAccountOwnership;
    }

    public TransactionStatisticTransformerFactory getTransformerFactory() {
        return transformerFactory;
    }

    public AggregateStatisticsRequest setTransformerFactory(TransactionStatisticTransformerFactory transformerFactory) {
        this.transformerFactory = transformerFactory;
        return this;
    }

    public AggregateStatisticsRequest setTransformationFunction(Function<Transaction, Statistic> transformationFunction) {
        this.transformationFunction = transformationFunction;
        return this;
    }

    public AggregateStatisticsRequest addGroupReduce(Function<Statistic, Integer> grouper,
            Function<Collection<Statistic>, Collection<Statistic>> reducer) {
        groupReducePairs.add(new GroupReducePair(grouper, reducer));
        return this;
    }

    public AggregateStatisticsRequest setDisregardAccountOwnership(boolean disregardAccountOwnership) {
        this.disregardAccountOwnership = disregardAccountOwnership;
        return this;
    }
}
