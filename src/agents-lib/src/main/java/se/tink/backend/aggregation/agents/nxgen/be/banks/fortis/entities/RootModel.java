package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RootModel {
    private ValueEntity value;
    private BusinessMessageBulkEntity businessMessageBulk;
}
