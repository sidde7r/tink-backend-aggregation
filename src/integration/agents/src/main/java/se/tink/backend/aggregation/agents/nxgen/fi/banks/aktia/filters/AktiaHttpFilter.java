package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.filters;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaConstants;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

import javax.ws.rs.core.MediaType;
import java.util.Objects;

public class AktiaHttpFilter extends Filter {
    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws HttpClientException, HttpResponseException {
        if (httpRequest.getHeaders().getFirst("Accept") == null) {
            httpRequest.getHeaders().add("Accept", MediaType.APPLICATION_JSON);
        }

        if (Objects.equals(HttpMethod.POST, httpRequest.getMethod()) &&
                httpRequest.getHeaders().getFirst("Content-Type") == null) {
            httpRequest.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON);
        }

        if (httpRequest.getUrl().get().contains(AktiaConstants.Authentication.ENCAP_AUTHORIZATION)) {
            httpRequest.getHeaders().remove("Authorization");
        }

        return nextFilter(httpRequest);
    }
}
