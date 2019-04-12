package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.ClientResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData.EsIdentityDataBuilder;

public class IngIdentityDataFetcher implements IdentityDataFetcher {
    private IngApiClient ingApiClient;

    public IngIdentityDataFetcher(IngApiClient ingApiClient) {
        this.ingApiClient = ingApiClient;
    }

    @Override
    public IdentityData fetchIdentityData() {
        ClientResponse client = ingApiClient.getApiRestClient();

        EsIdentityDataBuilder builder = EsIdentityData.builder();

        switch (client.getDocumentType()) {
            case IngConstants.UsernameTypes.NIF:
                builder.setNifNumber(client.getDocumentNumber());
                break;
            case IngConstants.UsernameTypes.NIE:
                builder.setNieNumber(client.getDocumentNumber());
                break;
            case IngConstants.UsernameTypes.PASSPORT:
                builder.setPassportNumber(client.getDocumentNumber());
                break;
        }

        return builder.addFirstNameElement(client.getName())
                .addSurnameElement(client.getFirstSurname())
                .addSurnameElement(client.getSecondSurname())
                .setDateOfBirth(LocalDate.parse(client.getBirthDate(), IngUtils.DATE_FORMATTER))
                .build();
    }
}
