package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.identitydata;

import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData;

public class IberCajaIdentityDataFetcher implements IdentityDataFetcher {

    private final IberCajaSessionStorage iberCajaSessionStorage;

    public IberCajaIdentityDataFetcher(IberCajaSessionStorage iberCajaSessionStorage) {

        this.iberCajaSessionStorage = iberCajaSessionStorage;
    }

    @Override
    public IdentityData fetchIdentityData() {
        EsIdentityData.EsIdentityDataBuilder builder = EsIdentityData.builder();

        return builder.setDocumentNumber(iberCajaSessionStorage.getDocumentNumber())
                .setFullName(iberCajaSessionStorage.getFullName())
                .setDateOfBirth(null)
                .build();
    }
}
