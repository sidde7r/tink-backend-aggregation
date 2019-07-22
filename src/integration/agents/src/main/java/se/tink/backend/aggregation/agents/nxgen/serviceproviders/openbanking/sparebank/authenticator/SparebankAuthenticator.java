package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.rpc.ScaResponse;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class SparebankAuthenticator {
    private final SparebankApiClient apiClient;

    public SparebankAuthenticator(SparebankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public URL buildAuthorizeUrl(String state) {
        try {
            apiClient.getScaRedirect(state);
        } catch (HttpResponseException e) {
            return new URL(e.getResponse().getBody(ScaResponse.class).getRedirectUri());
        }
        throw new RuntimeException();
    }

    public void setUpPsuAndSession(String psuId, String tppSessionId) {
        apiClient.setUpTppSessionIdAndPsuId(tppSessionId, psuId);
    }
}
