package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MessagesEntity {
    private String ind;
    private String code;
    private String text;
}
