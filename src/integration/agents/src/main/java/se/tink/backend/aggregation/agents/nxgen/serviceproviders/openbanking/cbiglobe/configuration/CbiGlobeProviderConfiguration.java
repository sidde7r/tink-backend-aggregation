package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CbiGlobeProviderConfiguration {
    private final String aspspCode;
    private final String aspspProductCode;
}
