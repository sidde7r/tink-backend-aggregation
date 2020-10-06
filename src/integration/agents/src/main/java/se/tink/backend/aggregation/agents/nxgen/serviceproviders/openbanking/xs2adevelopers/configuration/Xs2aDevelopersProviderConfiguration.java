package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Xs2aDevelopersProviderConfiguration {
    private String clientId;
    private String baseUrl;
    private String redirectUrl;
}
