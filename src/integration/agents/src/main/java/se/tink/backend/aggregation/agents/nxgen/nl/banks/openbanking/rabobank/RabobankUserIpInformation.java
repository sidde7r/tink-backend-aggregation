package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RabobankUserIpInformation {
    private final boolean manualRequest;
    private final String userIp;
}
