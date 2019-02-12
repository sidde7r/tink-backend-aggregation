package se.tink.backend.aggregation.configuration.integrations.abnamro;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AbnAmroInternetBankingConfiguration {
    @JsonProperty
    private String host;

    @JsonProperty
    private String authorizationToken;

    @JsonProperty
    private AbnAmroProductsConfiguration products = new AbnAmroProductsConfiguration();

    @JsonProperty
    private AbnAmroAccountUpdatesConfiguration accountUpdates = new AbnAmroAccountUpdatesConfiguration();

    public String getHost() {
        return host;
    }

    public String getAuthorizationToken() {
        return authorizationToken;
    }

    public AbnAmroProductsConfiguration getProducts() {
        return products;
    }

    public void setProducts(AbnAmroProductsConfiguration products) {
        this.products = products;
    }

    public AbnAmroAccountUpdatesConfiguration getAccountUpdates() {
        return accountUpdates;
    }

}
