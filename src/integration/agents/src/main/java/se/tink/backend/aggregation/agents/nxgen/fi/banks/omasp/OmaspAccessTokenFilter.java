package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp;

import java.util.Objects;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.rpc.OmaspBaseResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

// The access_token changes with every request. This filter picks up the access token from the
// response and assigns
// it to the next request.
public class OmaspAccessTokenFilter extends Filter {
    private final SessionStorage sessionStorage;
    private final Credentials credentials;

    public OmaspAccessTokenFilter(SessionStorage sessionStorage, Credentials credentials) {
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        if (sessionStorage.containsKey(OmaspConstants.Storage.ACCESS_TOKEN)) {
            httpRequest
                    .getHeaders()
                    .add("Authorization", sessionStorage.get(OmaspConstants.Storage.ACCESS_TOKEN));
        }

        HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() >= 200 && response.getStatus() < 400) {
            OmaspBaseResponse baseResponse = response.getBody(OmaspBaseResponse.class);
            if (!Objects.isNull(baseResponse.getToken())) {
                sessionStorage.put(
                        OmaspConstants.Storage.ACCESS_TOKEN,
                        baseResponse.getToken().getAccessToken());
            }
        }

        return response;
    }
}
