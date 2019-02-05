package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ActionsEntity {
    private String id;
    private boolean isActive;
    private String type;
    private int priority;
}
