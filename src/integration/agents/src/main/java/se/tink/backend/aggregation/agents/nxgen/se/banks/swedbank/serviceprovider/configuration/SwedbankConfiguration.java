package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.configuration;

import javax.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ProfileParameters;

@RequiredArgsConstructor
@Getter
public class SwedbankConfiguration {
    private final ProfileParameters profileParameters;
    private final String host;
    private final boolean hasPayments;

    public String getApiKey() {
        return profileParameters.getApiKey();
    }

    public String getName() {
        return profileParameters.getName();
    }

    public boolean isSavingsBank() {
        return profileParameters.isSavingsBank();
    }

    public boolean hasPayments() {
        return hasPayments;
    }

    @Nullable
    public String getUserAgent() {
        return profileParameters.getUserAgent();
    }
}
