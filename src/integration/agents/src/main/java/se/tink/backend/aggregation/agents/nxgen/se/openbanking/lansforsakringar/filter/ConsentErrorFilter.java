package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class ConsentErrorFilter extends Filter {
    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        try {
            return nextFilter(httpRequest);

        } catch (HttpResponseException e) {
            final HttpResponse response = e.getResponse();

            throwIfConsentError(response);
            throw e;
        }
    }

    private void throwIfConsentError(HttpResponse response) {
        final String errorMessage = response.getBody(String.class);
        if (response.getStatus() == HttpStatus.SC_UNAUTHORIZED
                && errorMessage.contains(LansforsakringarConstants.ErrorMessages.INVALID_TOKEN)) {
            throw SessionError.SESSION_EXPIRED.exception(response.getBody(String.class));
        }
    }
}
