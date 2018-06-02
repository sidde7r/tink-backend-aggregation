package se.tink.backend.grpc.v1.utils;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import com.google.common.collect.ImmutableList;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.UserProfile;
import se.tink.backend.grpc.v1.converter.statistic.StatisticNode;
import se.tink.libraries.date.DateUtils;

public class StatisticTreeBuilder {
    private final UserProfile userProfile;
    private final StatisticNode root;
    private long firstStatisticTimestamp = Long.MAX_VALUE;
    private long lastStatisticTimestamp = 0;

    private final static List<ResolutionTypes> parentNodeResolutionTypes = ImmutableList.of(
            ResolutionTypes.MONTHLY, ResolutionTypes.MONTHLY_ADJUSTED, ResolutionTypes.WEEKLY);

    public StatisticTreeBuilder(UserProfile userProfile) {
        this.userProfile = userProfile;
        this.root = new StatisticNode();
    }

    public StatisticTreeBuilder addNode(Statistic statistic) {
        return addNode(StatisticNode.convert(statistic, userProfile));
    }

    public StatisticTreeBuilder addNode(StatisticNode node) {
        if (node.getPeriod().getStartDate().getTime() < firstStatisticTimestamp) {
            firstStatisticTimestamp = node.getPeriod().getStartDate().getTime();
        }
        if (node.getPeriod().getEndDate().getTime() > lastStatisticTimestamp) {
            lastStatisticTimestamp = node.getPeriod().getEndDate().getTime();
        }
        return addNode(root, node);
    }

    private StatisticTreeBuilder addNode(StatisticNode ancestorNode, StatisticNode descendantNode) {
        if (isChildNode(ancestorNode, descendantNode)) {
            addOrUpdateNode(ancestorNode, descendantNode);
            return this;
        }

        String descendantAncestorPeriod = findDescendantAncestorPeriod(ancestorNode.getPeriod(),
                descendantNode.getPeriod());
        StatisticNode nextAncestor = ancestorNode.getChildren().get(descendantAncestorPeriod);

        if (nextAncestor == null) {
            nextAncestor = createChildNode(ancestorNode.getPeriod(), descendantAncestorPeriod);
            ancestorNode.putChild(descendantAncestorPeriod, nextAncestor);
        }

        addNode(nextAncestor, descendantNode);
        return this;
    }

    public StatisticNode build() {
        root.setPeriod(getOverallPeriod());
        calculateAmount(root);
        return root;
    }

    private Period getOverallPeriod() {
        Period period = new Period();
        period.setResolution(ResolutionTypes.ALL);
        period.setStartDate(new Date(firstStatisticTimestamp));
        period.setEndDate(new Date(lastStatisticTimestamp));

        return period;
    }

    private double calculateAmount(StatisticNode node) {

        if (node.getPeriod() != null && isWeek(node.getPeriod())) {
            return 0;
        }

        StatisticNode latestNode = getLatestNode(node);

        if (latestNode == null) {
            return node.getAmount();
        }

        return latestNode.getAmount();
    }

    private StatisticNode getLatestNode(StatisticNode node) {

        StatisticNode latestChild = null;
        for (StatisticNode child : node.getChildren().values()) {
            StatisticNode grandChild = getLatestNode(child);
            if (grandChild != null) {
                child.setAmount(grandChild.getAmount());
            }

            if (latestChild == null) {
                latestChild = child;
            }

            if (latestChild.getPeriod().getEndDate().before(child.getPeriod().getEndDate())) {
                latestChild = child;
            }
        }
        return latestChild;
    }

    /***
     * Add new descendantNode if it is not exist. Update descendantNode's description and amount if it was already in list
     */
    private void addOrUpdateNode(StatisticNode parentNode, StatisticNode childNode) {
        StatisticNode existed = parentNode.getChildren().get(childNode.getPeriod().getName());

        if (existed == null) {
            parentNode.putChild(childNode.getPeriod().getName(), childNode);
        } else {
            existed.setDescription(childNode.getDescription());
            existed.setAmount(childNode.getAmount());
        }
    }

    /***
     * By default, first node has only yearly descendantPeriod nodes, yearly descendantPeriod node - monthly descendantPeriod node. We do not create
     * manually daily or weekly nodes, we get them only from statistic request
     */
    private StatisticNode createChildNode(Period parentPeriod, String childPeriod) {
        StatisticNode node = new StatisticNode();

        node.setPeriod(UserProfile.ProfileDateUtils.buildPeriod(childPeriod, userProfile.getPeriodMode(), userProfile));

        return node;
    }

    /***
     * Return ancestor descendantPeriod
     */
    private String findDescendantAncestorPeriod(Period ancestorPeriod, Period descendantPeriod) {
        return DateUtils.getMonthPeriod(descendantPeriod.getStartDate(), userProfile.getPeriodMode(),
                userProfile.getPeriodAdjustedDay());
    }

    private boolean isChildNode(StatisticNode ancestorNode, StatisticNode descendantNode) {
        ResolutionTypes nodeResolution = descendantNode.getPeriod().getResolution();

        if (ancestorNode.getPeriod() == null && parentNodeResolutionTypes.contains(nodeResolution)) {
            return true;
        }

        if (ancestorNode.getPeriod() == null) {
            return false;
        }

        switch (ancestorNode.getPeriod().getResolution()) {
            case YEARLY:
                return Objects.equals(nodeResolution, ResolutionTypes.MONTHLY) ||
                        Objects.equals(nodeResolution, ResolutionTypes.MONTHLY_ADJUSTED) ||
                        Objects.equals(nodeResolution, ResolutionTypes.WEEKLY);
            case MONTHLY:
            case MONTHLY_ADJUSTED:
            case WEEKLY:
                return Objects.equals(nodeResolution, ResolutionTypes.DAILY);
            default:
                return false;
        }
    }

    private boolean isWeek(Period period) {
        return Objects.equals(period.getResolution(), ResolutionTypes.WEEKLY);
    }

}
