package se.tink.backend.aggregation.agents.nxgen.dk.banks.skjernbank;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConfiguration;

public class SkjernBankConfiguration implements BankDataConfiguration {
    @Override
    public String getAuthHost() {
        return "https://auth.skjernbank.dk";
    }

    @Override
    public String getHost() {
        return "https://api.skjernbank.dk";
    }

    @Override
    public String getUserAgent() {
        return "SkjernBank/2021.10.22 (iPhone; iOS 13.3.1; Scale/3.00)";
    }

    @Override
    public String getBuildNumber() {
        return "3251";
    }

    // API_KEY is retrieved from tracing the mobile app traffic in header "x-api-key"
    @Override
    public String getApiKey() {
        return "rel-apps-prod-6a75a955-4c47-4ac2-bead-919e4a9b28ca";
    }

    @Override
    public String getAppVersion() {
        return "2021.10.22.3251";
    }

    @Override
    public String getReferer() {
        return "https://auth.skjernbank.dk/authentication/nemid_bank_twofactor";
    }

    @Override
    public String getRedirectUri() {
        return "drb://drb.skjernbank.dk/dcr-callback.html";
    }
}
