package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.SigningIdHeaderDto;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EnrollDeviceRoundOneResponse {
    private SigningIdHeaderDto header;

    public SigningIdHeaderDto getHeader() {
        return header;
    }
}
