package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp;

import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.rpc.OmaspBaseResponse;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

// The access_token changes with every request. This filter picks up the access token from the
// response and assigns
// it to the next request.
public class OmaspAccessTokenFilter extends Filter {
    private final SessionStorage sessionStorage;

    public OmaspAccessTokenFilter(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
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
