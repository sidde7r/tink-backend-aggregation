package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CreditAgricoleBranchConfiguration {
    private final String baseUrl;
    private final String authorizeUrl;
}
