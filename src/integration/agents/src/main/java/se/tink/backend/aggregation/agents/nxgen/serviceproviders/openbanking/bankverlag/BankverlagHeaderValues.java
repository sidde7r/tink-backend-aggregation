package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class BankverlagHeaderValues {
    private final String aspspId;
    private final String userIp;
}
