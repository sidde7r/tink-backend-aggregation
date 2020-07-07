package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client;

import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class ExceptionFilter extends Filter {

    private static final String INVALID_GRANT = "invalid_grant";

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        HttpResponse httpResponse = nextFilter(httpRequest);
        if (httpResponse.getStatus() >= 400) {
            handleKnownErrors(httpResponse);
        }
        return httpResponse;
    }

    private void handleKnownErrors(HttpResponse httpResponse) {
        ErrorResponse body = httpResponse.getBody(ErrorResponse.class);
        if (body.getHttpStatus() == 400 && INVALID_GRANT.equals(body.getError())) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
