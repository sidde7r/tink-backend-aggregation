package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CrosskeyMarketConfiguration {
    private String financialId;
    private String baseApiURL;
    private String baseAuthURL;
}
