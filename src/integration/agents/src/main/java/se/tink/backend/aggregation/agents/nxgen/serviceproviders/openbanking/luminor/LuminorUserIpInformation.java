package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class LuminorUserIpInformation {
    private final boolean userPresent;
    private final String userIp;
}
