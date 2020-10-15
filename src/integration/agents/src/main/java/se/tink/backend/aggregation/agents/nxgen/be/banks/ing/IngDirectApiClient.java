package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.KeyAgreementRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.KeyAgreementResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.RemoteEvidenceSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.RemoteEvidenceSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.RemoteProfileMeansResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class IngDirectApiClient {

    private final TinkHttpClient httpClient;

    public IngDirectApiClient(TinkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public KeyAgreementResponse bootstrapKeys(KeyAgreementRequest request) {
        return httpClient
                .request(Urls.KEY_AGREEMENT)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(request)
                .post(KeyAgreementResponse.class);
    }

    public RemoteProfileMeansResponse getDeviceProfileMeans(String mobileAppId) {
        return httpClient
                .request(Urls.DEVICE_PROFILE_MEANS.parameter("id", mobileAppId))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(RemoteProfileMeansResponse.class);
    }

    public RemoteProfileMeansResponse getMpinProfileMeans(String mobileAppId) {
        return httpClient
                .request(Urls.MPIN_PROFILE_MEANS.parameter("id", mobileAppId))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(RemoteProfileMeansResponse.class);
    }

    public RemoteEvidenceSessionResponse createEvidence(
            String id, RemoteEvidenceSessionRequest request) {
        return httpClient
                .request(Urls.EVIDENCE_SESSION.parameter("id", id))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(request)
                .post(RemoteEvidenceSessionResponse.class);
    }
}
