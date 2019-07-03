package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.modelo;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31AisConfiguration;
import se.tink.backend.aggregation.nxgen.http.URL;

public class ModeloV31AisConfiguration extends UkOpenBankingV31AisConfiguration {

    public ModeloV31AisConfiguration(String apiBaseURL, String authBaseURL) {
        super(apiBaseURL, authBaseURL);
    }

    @Override
    public URL createConsentRequestURL() {
        return new URL(super.authBaseURL + "/aisp/account-access-consents");
    }
}
