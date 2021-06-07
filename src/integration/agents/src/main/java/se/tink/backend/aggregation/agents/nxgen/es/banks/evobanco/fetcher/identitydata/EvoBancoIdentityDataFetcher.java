package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.identitydata;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.identitydata.entities.EvoBancoIdentityEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.identitydata.rpc.EvoBancoIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.filter.entity.EvoBancoErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
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
        HttpResponse response = null;
        try {
            response = apiClient.fetchIdentityData();
        } catch (HttpResponseException e) {
            mapHttpError(e.getResponse());
        }
        EvoBancoIdentityDataResponse identityDataResponse =
                Objects.requireNonNull(response).getBody(EvoBancoIdentityDataResponse.class);
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

    private void mapHttpError(HttpResponse response) {
        EvoBancoErrorResponse errorResponse = response.getBody(EvoBancoErrorResponse.class);

        if (ErrorCodes.INVALID_TOKEN.equals(errorResponse.getResponse().getCode())) {
            throw SupplementalInfoError.NO_VALID_CODE.exception();
        }
        String message =
                String.format(
                        "Unknown error: httpStatus %s, code %s, message %s",
                        response.getStatus(),
                        errorResponse.getResponse().getCode(),
                        errorResponse.getResponse().getMessage());
        throw LoginError.DEFAULT_MESSAGE.exception(message);
    }
}
