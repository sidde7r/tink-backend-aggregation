package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator;

import java.util.Optional;
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
        Optional<URL> url = Optional.empty();
        try {
            apiClient.getScaRedirect(state);
        } catch (HttpResponseException e) {
            if (e.getResponse().getBody(String.class).contains("scaRedirect")) {
                url =
                        Optional.of(
                                new URL(
                                        e.getResponse()
                                                .getBody(ScaResponse.class)
                                                .getRedirectUri()));
            } else {
                throw e;
            }
        }
        return url.orElseThrow(() -> new IllegalStateException("SCA redirect missing"));
    }

    public void setUpPsuAndSession(String psuId, String tppSessionId) {
        apiClient.setPsuId(psuId);
        apiClient.setTppSessionId(tppSessionId);
    }
}
