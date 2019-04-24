package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.identitydata;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.rpc.UserDataResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData;

public class LaCaixaIdentityDataFetcher implements IdentityDataFetcher {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy");

    private final LaCaixaApiClient apiClient;

    public LaCaixaIdentityDataFetcher(LaCaixaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public IdentityData fetchIdentityData() {
        UserDataResponse userDataResponse = apiClient.fetchIdentityData();

        return EsIdentityData.builder()
                .setDocumentNumber(userDataResponse.getDNI())
                .addFirstNameElement(userDataResponse.getFirstName())
                .addSurnameElement(userDataResponse.getFirstSurName())
                .addSurnameElement(userDataResponse.getSecondSurName())
                .setDateOfBirth(LocalDate.parse(userDataResponse.getDateOfBirth(), DATE_FORMATTER))
                .build();
    }
}
