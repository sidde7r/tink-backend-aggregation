package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.rpc.IdentityDataResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData.EsIdentityDataBuilder;

public class BbvaIdentityDataFetcher implements IdentityDataFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(BbvaIdentityDataFetcher.class);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private final BbvaApiClient apiClient;

    public BbvaIdentityDataFetcher(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public IdentityData fetchIdentityData() {
        IdentityDataResponse identityDataResponse = apiClient.fetchIdentityData();
        final String documentNumber = identityDataResponse.getIdentityDocument().getNumber();
        final String documentType = identityDataResponse.getIdentityDocument().getType().getId();
        EsIdentityDataBuilder builder = EsIdentityData.builder();

        switch (documentType) {
            case BbvaConstants.IdTypeCodes.NIF:
                builder.setNifNumber(documentNumber);
                break;
            case BbvaConstants.IdTypeCodes.NIE:
                builder.setNieNumber(documentNumber);
                break;
            default:
                LOGGER.warn("ES BBVA: Unhandled document type: {}", documentType);
        }

        return builder.addFirstNameElement(identityDataResponse.getName())
                .addSurnameElement(identityDataResponse.getLastName())
                .addSurnameElement(identityDataResponse.getMothersLastName())
                .setDateOfBirth(
                        LocalDate.parse(identityDataResponse.getBirthDate(), DATE_FORMATTER))
                .build();
    }
}
