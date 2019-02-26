package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class Preferences {

    private String overallExpensesGraph;
    private String overallHelpLayer;
    private String newQueryFilter;

    public String getOverallExpensesGraph() {
        return overallExpensesGraph;
    }

    public String getOverallHelpLayer() {
        return overallHelpLayer;
    }

    public String getNewQueryFilter() {
        return newQueryFilter;
    }
}
