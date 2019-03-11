package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.credit.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RewardsItem {
    private String name;
    private String id;
    private int nonMonetaryValue;
}
