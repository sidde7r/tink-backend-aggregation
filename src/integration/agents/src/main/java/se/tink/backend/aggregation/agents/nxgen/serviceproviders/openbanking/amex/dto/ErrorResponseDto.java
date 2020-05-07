package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponseDto {

    private int code;

    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
