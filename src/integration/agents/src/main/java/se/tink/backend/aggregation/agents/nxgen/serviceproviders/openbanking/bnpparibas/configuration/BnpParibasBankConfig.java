package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BnpParibasBankConfig {
    private String authorizeUrl;
    private String tokenUrl;
    private String baseUrl;
}
