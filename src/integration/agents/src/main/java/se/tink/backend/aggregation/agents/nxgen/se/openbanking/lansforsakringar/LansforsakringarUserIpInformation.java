package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class LansforsakringarUserIpInformation {
    private final boolean manualRequest;
    private final String userIp;
}
