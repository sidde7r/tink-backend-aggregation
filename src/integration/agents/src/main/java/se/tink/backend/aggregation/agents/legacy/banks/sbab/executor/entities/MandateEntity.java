package se.tink.backend.aggregation.agents.banks.sbab.executor.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MandateEntity {
    private int identfier;
    private String status;
    private String type;
    private String orgnbr;
}
