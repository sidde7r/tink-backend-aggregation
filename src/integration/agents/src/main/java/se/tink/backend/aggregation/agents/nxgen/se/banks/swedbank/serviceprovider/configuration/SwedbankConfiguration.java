package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.configuration;

import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ProfileParameters;

@RequiredArgsConstructor
public class SwedbankConfiguration {
    private final ProfileParameters profileParameters;
    private final String host;

    public String getHost() {
        return host;
    }

    public String getApiKey() {
        return profileParameters.getApiKey();
    }

    public String getName() {
        return profileParameters.getName();
    }

    public boolean isSavingsBank() {
        return profileParameters.isSavingsBank();
    }

    @Nullable
    public String getUserAgent() {
        return profileParameters.getUserAgent();
    }
}
