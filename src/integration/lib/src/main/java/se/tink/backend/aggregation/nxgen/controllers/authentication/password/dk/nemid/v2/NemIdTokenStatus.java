package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class NemIdTokenStatus {

    private final String code;
    private final String message;
}
