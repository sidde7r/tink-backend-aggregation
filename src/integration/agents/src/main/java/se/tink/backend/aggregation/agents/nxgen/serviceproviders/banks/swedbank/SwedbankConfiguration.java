package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank;

import se.tink.backend.aggregation.configuration.ClientConfiguration;

public interface SwedbankConfiguration extends ClientConfiguration {
    String getApiKey();

    String getBankId();

    String getName();

    boolean isSavingsBank();
}
