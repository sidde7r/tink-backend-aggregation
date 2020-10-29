package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank;

import com.google.common.base.Preconditions;
import javax.annotation.Nullable;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ProfileParameters;

public class SwedbankSEConfiguration implements SwedbankConfiguration {
    private final ProfileParameters profileParameters;

    public SwedbankSEConfiguration(String bankProviderPayload) {
        this.profileParameters =
                Preconditions.checkNotNull(
                        SwedbankSEConstants.PROFILE_PARAMETERS.get(bankProviderPayload));
    }

    @Override
    public String getHost() {
        return SwedbankSEConstants.HOST;
    }

    @Override
    public String getApiKey() {
        return profileParameters.getApiKey();
    }

    @Override
    public String getName() {
        return profileParameters.getName();
    }

    @Override
    public boolean isSavingsBank() {
        return profileParameters.isSavingsBank();
    }

    @Override
    @Nullable
    public String getUserAgent() {
        return profileParameters.getUserAgent();
    }
}
