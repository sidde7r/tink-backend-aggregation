package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class AbnAmroUserIpInformation {
    private final boolean userPresent;
    private final String userIp;
}
