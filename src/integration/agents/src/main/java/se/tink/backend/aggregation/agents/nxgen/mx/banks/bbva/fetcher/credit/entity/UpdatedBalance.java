package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.credit.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UpdatedBalance {
    private double amount;
    private String currency;
}
