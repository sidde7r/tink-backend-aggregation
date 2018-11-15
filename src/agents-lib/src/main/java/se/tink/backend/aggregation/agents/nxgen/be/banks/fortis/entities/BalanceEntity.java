package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {
    private String amount;
    private String currency;
}
