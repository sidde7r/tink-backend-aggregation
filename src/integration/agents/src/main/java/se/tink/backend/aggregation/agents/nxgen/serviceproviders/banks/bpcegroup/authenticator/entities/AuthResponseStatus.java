package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthResponseStatus {
    AUTHENTICATION_SUCCESS("AUTHENTICATION_SUCCESS"),
    AUTHENTICATION_LOCKED("AUTHENTICATION_LOCKED"),
    AUTHENTICATION_FAILED("AUTHENTICATION_FAILED"),
    FAILED_AUTHENTICATION("FAILED_AUTHENTICATION");

    private String name;
}
