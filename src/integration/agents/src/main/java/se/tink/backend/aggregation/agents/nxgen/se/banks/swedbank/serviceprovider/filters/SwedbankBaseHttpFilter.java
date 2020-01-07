package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.filters;

import java.util.Objects;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class SwedbankBaseHttpFilter extends Filter {
    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        httpRequest.getHeaders().add("Accept", MediaType.APPLICATION_JSON);

        if (Objects.equals(HttpMethod.POST, httpRequest.getMethod())
                || Objects.equals(HttpMethod.PUT, httpRequest.getMethod())) {
            httpRequest.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON);
        }

        return nextFilter(httpRequest);
    }
}
