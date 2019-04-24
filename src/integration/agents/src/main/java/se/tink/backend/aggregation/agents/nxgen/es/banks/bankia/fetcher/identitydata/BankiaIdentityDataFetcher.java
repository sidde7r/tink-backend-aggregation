package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.identitydata;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.identitydata.entity.IdentityDataEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.identitydata.rpc.IdentityDataResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData;

public class BankiaIdentityDataFetcher implements IdentityDataFetcher {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a");

    private final BankiaApiClient apiClient;

    public BankiaIdentityDataFetcher(BankiaApiClient apiClient) {

        this.apiClient = apiClient;
    }

    @Override
    public IdentityData fetchIdentityData() {
        IdentityDataResponse identityDataResponse = apiClient.fetchIdentityData();
        IdentityDataEntity identityDataEntity = identityDataResponse.getIdentityData();

        return EsIdentityData.builder()
                .setDocumentNumber(identityDataEntity.getClientDocument())
                .addFirstNameElement(identityDataEntity.getFirstName())
                .addSurnameElement(identityDataEntity.getFirstSurName())
                .addSurnameElement(identityDataEntity.getSecondSurName())
                .setDateOfBirth(
                        LocalDate.parse(identityDataEntity.getDateOfBirth(), DATE_FORMATTER))
                .build();
    }
}
