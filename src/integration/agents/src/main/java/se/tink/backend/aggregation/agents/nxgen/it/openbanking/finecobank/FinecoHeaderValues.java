package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class FinecoHeaderValues {
    private final String redirectUrl;
    private final String userIp;
}
