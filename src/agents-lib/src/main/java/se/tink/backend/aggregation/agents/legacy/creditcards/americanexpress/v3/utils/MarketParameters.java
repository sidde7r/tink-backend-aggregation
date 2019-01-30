package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.utils;

public class MarketParameters {
    private String locale;
    private String appId;
    private String baseUrl;
    private String clientVersion;
    private boolean useOldBankId;

    public boolean isUseOldBankId() {
        return useOldBankId;
    }

    public MarketParameters(String locale, String appId, String baseUrl, String clientVersion, boolean useOldBankId) {
        this.locale = locale;
        this.appId = appId;
        this.baseUrl = baseUrl;
        this.clientVersion = clientVersion;
        this.useOldBankId = useOldBankId;
    }

    public String getLocale() {
        return locale;
    }

    public String getAppId() {
        return appId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getClientVersion() {
        return clientVersion;
    }
}