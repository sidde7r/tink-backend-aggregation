package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2;

import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

public interface SebKortConfiguration extends ClientConfiguration {
    String getApiKey();

    String getProviderCode();

    String getProductCode();

    String getBankIdMethod();
}
