package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort;

public interface SebKortConfiguration {
    String getApiKey();

    String getProviderCode();

    String getProductCode();

    String getBankIdMethod();
}
