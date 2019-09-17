package se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;

public class DanskeBankNOConstants extends DanskeBankConstants {

    /**
     * Other markets are using "https://apiebank.danskebank.com", but DK and NO uses
     * "https://apiebank3.danskebank.com"
     *
     * @return host to be used for the bank API
     */
    @Override
    public String getHostUrl() {
        return "https://apiebank3.danskebank.com";
    }
}
