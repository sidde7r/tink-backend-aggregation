package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher;

import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

public class UkOpenBankingIdentityDataFetcher<IdentityDataResponseType>
        implements IdentityDataFetcher {

    private UkOpenBankingApiClient apiClient;
    private final Class<IdentityDataResponseType> responseType;
    private final UkOpenBankingAisConfig agentConfig;

    public UkOpenBankingIdentityDataFetcher(
            UkOpenBankingApiClient apiClient,
            UkOpenBankingAisConfig agentConfig,
            Class<IdentityDataResponseType> responseType) {
        this.apiClient = apiClient;
        this.responseType = responseType;
        this.agentConfig = agentConfig;
    }

    @Override
    public IdentityData fetchIdentityData() {

        String name =
                Objects.isNull(agentConfig.getIdentityData())
                        ? agentConfig.getHolderName()
                        : agentConfig.getIdentityData().getName();

        return IdentityData.builder().setFullName(name).setDateOfBirth(null).build();
    }
}
