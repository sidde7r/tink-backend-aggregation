package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignTypesInfoDto {
    private TypeValuePair title;

    public TypeValuePair getTitle() {
        return title;
    }
}
