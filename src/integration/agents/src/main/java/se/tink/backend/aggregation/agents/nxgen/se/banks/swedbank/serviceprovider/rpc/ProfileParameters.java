package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import javax.annotation.Nullable;

public class ProfileParameters {
    private final String name;
    private final String apiKey;
    private final boolean savingsBank;
    private final String userAgent;

    public ProfileParameters(String name, String apiKey, boolean savingsBank, String userAgent) {
        this.name = name;
        this.apiKey = apiKey;
        this.savingsBank = savingsBank;
        this.userAgent = userAgent;
    }

    public String getName() {
        return name;
    }

    public String getApiKey() {
        return apiKey;
    }

    public boolean isSavingsBank() {
        return savingsBank;
    }

    @Nullable
    public String getUserAgent() {
        return userAgent;
    }
}
