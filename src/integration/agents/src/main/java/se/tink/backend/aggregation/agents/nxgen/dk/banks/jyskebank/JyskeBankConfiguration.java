package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConfiguration;

public class JyskeBankConfiguration implements BankDataConfiguration {
    @Override
    public String getAuthHost() {
        return "https://auth.jyskebank.dk";
    }

    @Override
    public String getHost() {
        return "https://api.jyskebank.dk";
    }

    @Override
    public String getUserAgent() {
        return "JyskeBank/2.26.1 (iPhone; iOS 13.3.1; Scale/3.00)";
    }

    @Override
    public String getBuildNumber() {
        return "121";
    }

    // API_KEY is retrieved from tracing the mobile app traffic in header "x-api-key"
    @Override
    public String getApiKey() {
        return "w6FW248sXt42WZaaq8boFmXMGGTu06AG";
    }

    @Override
    public String getAppVersion() {
        return "2.26.1.121";
    }

    @Override
    public String getReferer() {
        return "https://auth.jyskebank.dk/authentication/nemid_bank_twofactor";
    }

    @Override
    public String getRedirectUri() {
        return "drb://drb.jyskebank.dk/dcr-callback.html";
    }
}
