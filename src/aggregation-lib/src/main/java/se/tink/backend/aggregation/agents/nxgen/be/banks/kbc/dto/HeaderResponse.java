package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HeaderResponse {
    private HeaderDto header;

    public HeaderDto getHeader() {
        return header;
    }
}
