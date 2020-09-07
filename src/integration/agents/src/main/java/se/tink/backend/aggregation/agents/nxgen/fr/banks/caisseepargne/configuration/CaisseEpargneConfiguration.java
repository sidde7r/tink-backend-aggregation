package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.configuration;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.configuration.BpceConfiguration;

@Getter
public class CaisseEpargneConfiguration implements BpceConfiguration {

    private final String authBaseUrl = "https://www.as-ex-ath-groupe.caisse-epargne.fr";
    private final String authHeaderValue = "";
    private final String authUserAgent = "CaisseEpargne/742 CFNetwork/978.0.7 Darwin/18.7.0";
    private final String branchId = "ce";
    private final String clientId = "f4ee2144-0d68-4b90-ae78-25e255a1f3ac";
    private final String clientSecret = "34791847-2cfe-4992-bff2-1c3327b92fab";
    private final String icgAuthBaseUrl = "https://www.icgauth.caisse-epargne.fr";
    private final String rsExAthBaseUrl = "https://www.rs-ex-ath-groupe.caisse-epargne.fr";
}
