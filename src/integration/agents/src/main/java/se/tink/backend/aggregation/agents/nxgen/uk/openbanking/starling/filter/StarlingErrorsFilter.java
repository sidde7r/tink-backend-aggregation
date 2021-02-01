package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.filter;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.rpc.StarlingErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;

@RequiredArgsConstructor
public class StarlingErrorsFilter extends Filter {
    private final PersistentStorage persistentStorage;
    private Credentials credentials;

    public StarlingErrorsFilter(PersistentStorage persistentStorage, Credentials credentials) {
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == HttpStatus.SC_FORBIDDEN) {

            StarlingErrorResponse errorResponse = response.getBody(StarlingErrorResponse.class);
            if (errorResponse.isInsufficientScope()) {
                forceReAuth(response);
            }

        } else if (response.getStatus() == HttpStatus.SC_BAD_REQUEST) {

            StarlingErrorResponse errorResponse = response.getBody(StarlingErrorResponse.class);
            if (errorResponse.isInvalidGrant()) {
                forceReAuth(response);
            }
        }

        return response;
    }

    private void forceReAuth(HttpResponse response) {
        removeOauthToken();
        credentials.setStatus(CredentialsStatus.AUTHENTICATION_ERROR);
        throw AuthorizationError.UNAUTHORIZED.exception(formatErrorDescription(response));
    }

    private void removeOauthToken() {
        persistentStorage.remove("CONSENT");
        persistentStorage.remove("OAUTH2_TOKEN");
    }

    private String formatErrorDescription(HttpResponse response) {
        StarlingErrorResponse errorResponse = response.getBody(StarlingErrorResponse.class);
        return "\n"
                + "Response statusCode: "
                + response.getStatus()
                + "\n"
                + "errorCode: "
                + errorResponse.getErrorCode()
                + "\n"
                + "errorDescription: "
                + errorResponse.getErrorDescription();
    }
}
