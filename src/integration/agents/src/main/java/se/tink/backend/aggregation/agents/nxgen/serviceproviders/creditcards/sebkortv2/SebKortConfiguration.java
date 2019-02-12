package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2;

public interface SebKortConfiguration {
    String getApiKey();

    String getProviderCode();

    String getProductCode();

    String getBankIdMethod();
}
