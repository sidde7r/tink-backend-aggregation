package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.configuration;

import javax.annotation.Nullable;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

public interface SwedbankConfiguration extends ClientConfiguration {

    String getHost();

    String getApiKey();

    String getBankId();

    String getName();

    boolean isSavingsBank();

    @Nullable
    String getUserAgent();
}
