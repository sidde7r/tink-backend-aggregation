package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IngUserAuthenticationData {

    private final boolean isManualAuthentication;
    private final String psuIdAddress;
}
