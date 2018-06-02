package se.tink.backend.aggregation.agents.banks.nordea;

public class MarketParameters {

    private static final String BASE_URL = "mobilebank.prod.nordea.com";
    private String authenticationEndPoint;
    private String bankingEndpoint;
    private String savingsEndpoint;
    private String marketCode;
    private String currency;
    private String appVersion;
    private ApiVersion apiVersion;

    public MarketParameters(String marketCode, ApiVersion apiVersion, String currency, String appVersion) {
        this.marketCode = marketCode;
        this.currency = currency;
        this.appVersion = appVersion;
        this.apiVersion = apiVersion;

        this.authenticationEndPoint = String.format("%s/AuthenticationService%s", baseUrl(marketCode), apiVersion.get());
        this.bankingEndpoint = String.format("%s/BankingService%s", baseUrl(marketCode), apiVersion.get());
        this.savingsEndpoint = String.format("%s/SavingsService%s", baseUrl(marketCode), apiVersion.get());
    }

    private String baseUrl(String marketCode) {
        return String.format("https://%s.%s/%s", marketCode.toLowerCase(), BASE_URL, marketCode.toUpperCase());
    }

    public String getAuthenticationEndPoint() {
        return authenticationEndPoint;
    }

    public String getBankingEndpoint() {
        return bankingEndpoint;
    }

    public String getSavingsEndpoint() {
        return savingsEndpoint;
    }

    public String getMarketCode() {
        return marketCode;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public ApiVersion getApiVersion() {
        return apiVersion;
    }
}
