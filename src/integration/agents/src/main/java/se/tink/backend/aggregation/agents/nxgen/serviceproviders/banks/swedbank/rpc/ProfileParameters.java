package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

public class ProfileParameters {
    private final String name;
    private final String apiKey;
    private final boolean savingsBank;

    public ProfileParameters(String name, String apiKey, boolean savingsBank) {
        this.name = name;
        this.apiKey = apiKey;
        this.savingsBank = savingsBank;
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
}
