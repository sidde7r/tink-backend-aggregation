package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionDateEntity {

    private String from;
    private String to;

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public TransactionDateEntity(String from, String to) {
        this.from = from;
        this.to = to;
    }
}
