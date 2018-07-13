package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SysDataEntity {
    private String categoryCodingSchemeVersion;

    public String getCategoryCodingSchemeVersion() {
        return categoryCodingSchemeVersion;
    }
}
