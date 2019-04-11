package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.ClientResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.customerinfo.CustomerInfoFetcher;
import se.tink.libraries.customerinfo.IdentityData;
import se.tink.libraries.customerinfo.countries.EsCustomerInfo;

public class IngCustomerInfoFetcher implements CustomerInfoFetcher {
    private IngApiClient ingApiClient;

    public IngCustomerInfoFetcher(IngApiClient ingApiClient) {
        this.ingApiClient = ingApiClient;
    }

    @Override
    public IdentityData fetchCustomerInfo() {
        ClientResponse client = ingApiClient.getApiRestClient();

        EsCustomerInfo.EsCustomerInfoBuilder builder = EsCustomerInfo.builder();

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
