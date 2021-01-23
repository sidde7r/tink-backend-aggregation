package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.filter;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.rpc.StarlingErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class StarlingErrorFilter extends Filter {
    private final PersistentStorage persistentStorage;

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == HttpStatus.SC_FORBIDDEN) {
            StarlingErrorResponse errorResponse = response.getBody(StarlingErrorResponse.class);
            if (errorResponse.isInsufficientScope()) {
                removeOauthToken();
                throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception();
            }
        }
        return response;
    }

    private void removeOauthToken() {
        persistentStorage.remove("CONSENT");
        persistentStorage.remove("OAUTH2_TOKEN");
    }
}
