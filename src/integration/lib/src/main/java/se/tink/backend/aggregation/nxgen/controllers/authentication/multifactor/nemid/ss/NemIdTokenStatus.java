package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class NemIdTokenStatus {

    private final String code;
    private final String message;
    private final String requestIssuer;
}
