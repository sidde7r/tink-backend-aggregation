package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.identitydata;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.SantanderEsIdNumberUtils.getIdNumberType;

import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.SantanderEsIdNumberUtils.IdNumberTypes;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData.EsIdentityDataBuilder;

public class SantanderEsIdentityDataFetcher implements IdentityDataFetcher {

    private final SantanderEsSessionStorage santanderEsSessionStorage;

    public SantanderEsIdentityDataFetcher(SantanderEsSessionStorage santanderEsSessionStorage) {
        this.santanderEsSessionStorage = santanderEsSessionStorage;
    }

    @Override
    public IdentityData fetchIdentityData() {
        final LoginResponse loginResponse = santanderEsSessionStorage.getLoginResponse();
        final String userId = santanderEsSessionStorage.getUserId();
        EsIdentityDataBuilder builder = EsIdentityData.builder();
        if (getIdNumberType(userId) == IdNumberTypes.NIF) {
            builder.setNifNumber(userId);
        } else if (getIdNumberType(userId) == IdNumberTypes.NIF) {
            builder.setNieNumber(userId);
        } else {
            builder.setPassportNumber(userId);
        }
        return builder.addFirstNameElement(loginResponse.getNameWithoutSurname())
                .addSurnameElement(loginResponse.getFirstSurname())
                .addSurnameElement(loginResponse.getSecondSurname())
                .setDateOfBirth(loginResponse.getDateOfBirth())
                .build();
    }

    public FetchIdentityDataResponse response() {
        return new FetchIdentityDataResponse(fetchIdentityData());
    }
}
