package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class HandelsbankenUserIpInformation {
    private final boolean userPresent;
    private final String userIp;
}
