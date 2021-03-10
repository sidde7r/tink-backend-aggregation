package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.identity;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.rpc.IdentityResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.identitydata.IdentityData;

public class OpenbankIdentityDataFetcher implements IdentityDataFetcher {

    private final OpenbankApiClient apiClient;

    public OpenbankIdentityDataFetcher(OpenbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public IdentityData fetchIdentityData() {
        try {
            IdentityResponse endUserIdentityResponse = apiClient.getUserIdentity();
            return endUserIdentityResponse.toTinkIdentity();
        } catch (HttpResponseException exception) {
            if (exception.getResponse().getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(exception);
            }
            throw exception;
        }
    }

    public FetchIdentityDataResponse response() {
        return new FetchIdentityDataResponse(fetchIdentityData());
    }
}
