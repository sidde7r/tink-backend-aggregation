package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BnpParibasFortisBaseBankConfiguration {

    private String baseUrl;
    private String baseAuth;
}
