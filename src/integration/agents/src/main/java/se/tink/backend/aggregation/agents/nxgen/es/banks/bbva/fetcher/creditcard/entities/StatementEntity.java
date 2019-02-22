package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StatementEntity {
    private String id;
    private String initialDate;
    private String endDate;
    private String statementDate;
}
