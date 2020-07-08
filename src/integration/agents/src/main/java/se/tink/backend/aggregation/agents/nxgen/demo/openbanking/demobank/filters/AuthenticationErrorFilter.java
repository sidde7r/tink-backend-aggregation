package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.filters;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class AuthenticationErrorFilter extends Filter {

    @lombok.SneakyThrows
    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);
        if (response.getStatus() == HttpStatus.SC_FORBIDDEN
                || response.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
            throw AuthorizationError.UNAUTHORIZED.exception(
                    "Authentication Error, received status "
                            + response.getStatus()
                            + " from Demo Bank");
        }
        return response;
    }
}
