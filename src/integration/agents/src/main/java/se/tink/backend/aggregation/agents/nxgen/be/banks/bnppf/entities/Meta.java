package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Meta {
    private int totalPages;
    private int totalTransactions;

    public int getTotalPages() {
        return totalPages;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }
}
