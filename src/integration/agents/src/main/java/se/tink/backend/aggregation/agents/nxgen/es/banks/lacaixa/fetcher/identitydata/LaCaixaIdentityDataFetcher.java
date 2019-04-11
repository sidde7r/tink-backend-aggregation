package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.identitydata;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.rpc.UserDataResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.customerinfo.CustomerInfoFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData.EsIdentityDataBuilder;

public class LaCaixaIdentityDataFetcher implements CustomerInfoFetcher {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy");

    private final LaCaixaApiClient apiClient;

    public LaCaixaIdentityDataFetcher(LaCaixaApiClient apiClient) {

        this.apiClient = apiClient;
    }

    @Override
    public IdentityData fetchCustomerInfo() {
        UserDataResponse userDataResponse = apiClient.fetchIdentityData();

        EsIdentityDataBuilder builder = EsIdentityData.builder();

        String dni = userDataResponse.getDNI();

        // DNI that starts with a letter is a NIE, identity document for foreigners
        if (dni.matches("^[a-zA-Z].*$")) {
            builder.setNieNumber(dni);
        } else {
            builder.setNifNumber(dni);
        }

        return builder.addFirstNameElement(userDataResponse.getFirstName())
                .addSurnameElement(userDataResponse.getFirstSurName())
                .addSurnameElement(userDataResponse.getSecondSurName())
                .setDateOfBirth(LocalDate.parse(userDataResponse.getDateOfBirth(), DATE_FORMATTER))
                .build();
    }
}
