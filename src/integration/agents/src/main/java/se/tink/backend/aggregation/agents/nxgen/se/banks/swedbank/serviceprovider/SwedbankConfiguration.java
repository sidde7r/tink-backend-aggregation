package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider;

import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

public interface SwedbankConfiguration extends ClientConfiguration {
    String getApiKey();

    String getBankId();

    String getName();

    boolean isSavingsBank();
}
