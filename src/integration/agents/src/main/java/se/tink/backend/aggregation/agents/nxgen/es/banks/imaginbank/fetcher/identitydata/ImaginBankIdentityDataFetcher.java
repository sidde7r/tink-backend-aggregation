package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.identitydata;

import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.identitydata.rpc.UserDataResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData;

public class ImaginBankIdentityDataFetcher implements IdentityDataFetcher {

    private final ImaginBankSessionStorage imaginBankSessionStorage;
    private final ImaginBankApiClient apiClient;

    public ImaginBankIdentityDataFetcher(
            ImaginBankSessionStorage imaginBankSessionStorage, ImaginBankApiClient apiClient) {
        this.imaginBankSessionStorage = imaginBankSessionStorage;
        this.apiClient = apiClient;
    }

    @Override
    public IdentityData fetchIdentityData() {
        LoginResponse loginResponse = imaginBankSessionStorage.getLoginResponse();
        UserDataResponse userDataResponse = apiClient.fetchDni();
        String dni = userDataResponse.getDni();

        EsIdentityData.EsIdentityDataBuilder builder = EsIdentityData.builder();

        return builder.setDocumentNumber(dni)
                .setFullName(loginResponse.getName())
                .setDateOfBirth(loginResponse.getFormattedDateOfBirth())
                .build();
    }
}
