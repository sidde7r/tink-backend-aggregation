package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class FabricUserIpInformation {
    private final boolean manualRequest;
    private final String userIp;
}
