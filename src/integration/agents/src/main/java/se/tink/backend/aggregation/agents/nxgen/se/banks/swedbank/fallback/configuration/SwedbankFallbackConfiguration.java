package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.configuration;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import javax.annotation.Nullable;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.SwedbankFallbackConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ProfileParameters;

public class SwedbankFallbackConfiguration implements SwedbankConfiguration {

    private final ProfileParameters profileParameters;

    public SwedbankFallbackConfiguration(String bankProviderPayload) {
        this.profileParameters =
                Preconditions.checkNotNull(
                        SwedbankFallbackConstants.PROFILE_PARAMETERS.get(bankProviderPayload));
    }

    @Override
    public String getHost() {
        return SwedbankFallbackConstants.HOST;
    }

    @Override
    public String getApiKey() {
        return profileParameters.getApiKey();
    }

    @Override
    public String getBankId() {
        return null;
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
        return null;
    }
}
