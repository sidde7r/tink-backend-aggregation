package se.tink.backend.aggregation.agents.nxgen.fr.banks.hellobank;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.BnpParibasConfigurationBase;

@Getter
public class HelloBankConfiguration implements BnpParibasConfigurationBase {

    private final String host = "https://m-webservices.hellobank.fr/";
    private final String userAgent = "HelloBank/3.6.1.0 CFNetwork/1121.2.2 Darwin/19.3.0";
    private final String gridType = "helloBankV2iOS";
    private final String distId = "HelloBank";
    private final String appVersion = "3.6.1";
    private final String buildNumber = "3.6.1.0";
    private final String numpadLastDigitIndex = "10";
    private final String triAvValue = "0";
    private final String pastOrPendingValue = "1";
}
