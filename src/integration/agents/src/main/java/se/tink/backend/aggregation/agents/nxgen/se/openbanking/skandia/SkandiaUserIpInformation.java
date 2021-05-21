package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SkandiaUserIpInformation {
    private final boolean manualRequest;
    private final String userIp;
}
