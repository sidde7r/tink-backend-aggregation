package se.tink.backend.grpc.v1.converter.statistic;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import java.util.Map;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.date.Period;

public class StatisticNode {
    private String description;
    private Period period;
    private double amount;
    private Map<String, StatisticNode> children = Maps.newHashMap();

    public static StatisticNode convert(Statistic statistic, UserProfile userProfile) {
        StatisticNode node = new StatisticNode();
        node.setDescription(statistic.getDescription());
        node.setPeriod(UserProfile.ProfileDateUtils.buildPeriod(statistic.getPeriod(), statistic.getResolution(), userProfile));
        node.setAmount(statistic.getValue());

        return node;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Map<String, StatisticNode> getChildren() {
        return children;
    }

    public void setChildren(Map<String, StatisticNode> children) {
        this.children = children;
    }

    public void putChild(String key, StatisticNode child) {
        this.children.put(key, child);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("description", description)
                .add("period", period)
                .add("amount", amount)
                .add("children", children)
                .toString();
    }
}
