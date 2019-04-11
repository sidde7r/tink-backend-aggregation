package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.fetcher;

import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvApiClient;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.GetUserDataResponse;

public class AsLhvAccountFetcher {
    protected final AsLhvApiClient apiClient;

    public AsLhvAccountFetcher(final AsLhvApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public GetUserDataResponse fetchUserData() {
        GetUserDataResponse userDataResponse = apiClient.getUserData();
        if (userDataResponse.requestFailed()) {
            final String errorMessage =
                    String.format(
                            "Failed to fetch user data: %s", userDataResponse.getErrorMessage());
            throw new IllegalStateException(errorMessage);
        }
        return userDataResponse;
    }
}
