package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas;

import lombok.Getter;

@Getter
public class BnpParibasConfiguration implements BnpParibasConfigurationBase {

    private final String host = "https://m-service.bnpparibas.net/";
    private final String userAgent = "MesComptes/396 CFNetwork/1121.2.2 Darwin/19.3.0";
    private final String gridType = "mesComptesV4iOS_MOB";
    private final String distId = "BNPNetParticulier";
    private final String appVersion = "4.24.1";
    private final String buildNumber = "396";
    private final String numpadLastDigitIndex = "11";
    private final String triAvValue = "1";
    private final String pastOrPendingValue = "2";
}
