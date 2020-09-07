package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.configuration;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.configuration.BpceConfiguration;

@Getter
public class BanquePopulaireConfiguration implements BpceConfiguration {

    private final String authBaseUrl = "https://www.as-ex-ath-groupe.banquepopulaire.fr";
    private final String authHeaderValue =
            "BP_cyberplus.mobile.ios_PROD_3.35:04ef0098-f90a-46cd-8791-48578a92f99d";
    private final String authUserAgent = "BanquePopulaire/78.5 CFNetwork/1121.2.2 Darwin/19.3.0";
    private final String branchId = "bp";
    private final String clientId = "08767475-1d17-4bac-93e7-78ea65c7d794";
    private final String clientSecret = "f42ead04-f927-42de-ab1c-6c2809d6c62d";
    private final String icgAuthBaseUrl = "https://www.icgauth.banquepopulaire.fr";
    private final String rsExAthBaseUrl = "https://www.rs-ex-ath-groupe.banquepopulaire.fr";
}
