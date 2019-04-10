package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.ProfileParameters;

public class SwedbankSEConfiguration implements SwedbankConfiguration {
    private final ProfileParameters profileParameters;

    public SwedbankSEConfiguration(String bankProviderPayload) {
        this.profileParameters =
                Preconditions.checkNotNull(
                        SwedbankSEConstants.PROFILE_PARAMETERS.get(bankProviderPayload));
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

    public boolean isSavingsBank() {
        return profileParameters.isSavingsBank();
    }
}
