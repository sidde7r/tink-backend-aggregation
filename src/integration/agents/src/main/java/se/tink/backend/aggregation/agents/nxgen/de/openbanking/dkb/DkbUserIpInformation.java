package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class DkbUserIpInformation {

    private final boolean userPresent;
    private final String userIp;
}
