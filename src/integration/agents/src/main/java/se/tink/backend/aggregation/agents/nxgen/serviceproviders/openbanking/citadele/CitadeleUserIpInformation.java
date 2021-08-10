package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CitadeleUserIpInformation {
    private final boolean userPresent;
    private final String userIp;
}
