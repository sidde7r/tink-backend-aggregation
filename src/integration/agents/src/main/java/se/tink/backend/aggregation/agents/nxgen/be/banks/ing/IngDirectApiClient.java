package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.KeyAgreementRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.KeyAgreementResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.RemoteEvidenceSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.RemoteEvidenceSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.RemoteProfileMeansResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.BackOffProvider;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.IngLoggingAdapter;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.IngRetryFilter;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class IngDirectApiClient {

    private final TinkHttpClient httpClient;

    private final DirectLoggingFilter directLoggingFilter;

    private final IngRetryFilter retryFilter;

    public IngDirectApiClient(TinkHttpClient httpClient, IngLoggingAdapter ingLoggingAdapter) {
        this.httpClient = httpClient;
        this.directLoggingFilter = new DirectLoggingFilter(ingLoggingAdapter);
        this.retryFilter =
                new IngRetryFilter(
                        IngConstants.MAX_RETRIES,
                        new BackOffProvider(IngConstants.THROTTLING_DELAY));
    }

    public KeyAgreementResponse bootstrapKeys(KeyAgreementRequest request) {
        return httpClient
                .request(Urls.KEY_AGREEMENT)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .addFilter(retryFilter)
                .addFilter(directLoggingFilter)
                .body(request)
                .post(KeyAgreementResponse.class);
    }

    public RemoteProfileMeansResponse getDeviceProfileMeans(String mobileAppId) {
        return httpClient
                .request(Urls.DEVICE_PROFILE_MEANS.parameter("id", mobileAppId))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .addFilter(retryFilter)
                .addFilter(directLoggingFilter)
                .get(RemoteProfileMeansResponse.class);
    }

    public RemoteProfileMeansResponse getMpinProfileMeans(String mobileAppId) {
        return httpClient
                .request(Urls.MPIN_PROFILE_MEANS.parameter("id", mobileAppId))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .addFilter(retryFilter)
                .addFilter(directLoggingFilter)
                .get(RemoteProfileMeansResponse.class);
    }

    public RemoteEvidenceSessionResponse createEvidence(
            String id, RemoteEvidenceSessionRequest request) {
        return httpClient
                .request(Urls.EVIDENCE_SESSION.parameter("id", id))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(request)
                .addFilter(retryFilter)
                .addFilter(directLoggingFilter)
                .post(RemoteEvidenceSessionResponse.class);
    }

    @FilterOrder(category = FilterPhases.SEND, order = 0)
    private static class DirectLoggingFilter extends Filter {

        private final IngLoggingAdapter ingLoggingAdapter;

        public DirectLoggingFilter(IngLoggingAdapter ingLoggingAdapter) {
            this.ingLoggingAdapter = ingLoggingAdapter;
        }

        @Override
        public HttpResponse handle(HttpRequest httpRequest)
                throws HttpClientException, HttpResponseException {
            ingLoggingAdapter.logRequest(httpRequest);
            HttpResponse response = nextFilter(httpRequest);
            ingLoggingAdapter.logResponse(response);
            return response;
        }
    }
}
