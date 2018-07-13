package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignValidationResponse {
    private SigningIdHeaderDto header;

    public SigningIdHeaderDto getHeader() {
        return header;
    }
}
