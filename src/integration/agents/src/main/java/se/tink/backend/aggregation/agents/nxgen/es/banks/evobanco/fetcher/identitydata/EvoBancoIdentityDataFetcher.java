package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.identitydata;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.identitydata.entities.EvoBancoIdentityEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.identitydata.rpc.EvoBancoIdentityDataResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData;

public class EvoBancoIdentityDataFetcher implements IdentityDataFetcher {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final EvoBancoApiClient apiClient;

    public EvoBancoIdentityDataFetcher(EvoBancoApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public IdentityData fetchIdentityData() {
        EvoBancoIdentityDataResponse identityDataResponse = apiClient.fetchIdentityData();
        EvoBancoIdentityEntity identityEntity =
                identityDataResponse.getClientData().getIdentityEntity();
        return EsIdentityData.builder()
                .setDocumentNumber(identityEntity.getDocumentId())
                .addFirstNameElement(identityEntity.getName())
                .addSurnameElement(identityEntity.getFirstSurname())
                .addSurnameElement(identityEntity.getSecondSurname())
                .setDateOfBirth(LocalDate.parse(identityEntity.getDateOfBirth(), DATE_FORMATTER))
                .build();
    }
}
