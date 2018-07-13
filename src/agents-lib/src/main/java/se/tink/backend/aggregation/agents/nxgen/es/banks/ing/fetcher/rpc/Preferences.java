package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class Preferences {

    private String overallExpensesGraph;
    private String overallHelpLayer;
    private String newQueryFilter;

    public String getOverallExpensesGraph() {
        return overallExpensesGraph;
    }

    public void setOverallExpensesGraph(String overallExpensesGraph) {
        this.overallExpensesGraph = overallExpensesGraph;
    }

    public String getOverallHelpLayer() {
        return overallHelpLayer;
    }

    public void setOverallHelpLayer(String overallHelpLayer) {
        this.overallHelpLayer = overallHelpLayer;
    }

    public String getNewQueryFilter() {
        return newQueryFilter;
    }

    public void setNewQueryFilter(String newQueryFilter) {
        this.newQueryFilter = newQueryFilter;
    }
}
