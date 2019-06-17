package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.danskebank;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Configuration;
import se.tink.backend.aggregation.nxgen.http.URL;

public class DanskeBankOpenBankingV31Configuration extends UkOpenBankingV31Configuration {

    @Override
    public URL createConsentRequestURL(URL authBaseURL) {
        return new URL(authBaseURL + "/account-access-consents");
    }
}
