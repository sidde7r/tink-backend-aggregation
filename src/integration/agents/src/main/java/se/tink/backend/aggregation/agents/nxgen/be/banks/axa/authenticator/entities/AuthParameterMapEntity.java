package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthParameterMapEntity {

    private ParameterEntry parameterEntry;

    @JsonObject
    public static class ParameterEntry {
        private String parameterType;
        private String value;
    }
}
