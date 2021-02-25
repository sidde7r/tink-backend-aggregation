package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OperationCardLimitEntity {
    private boolean level;
    private String scope;
    private String limit;
}
