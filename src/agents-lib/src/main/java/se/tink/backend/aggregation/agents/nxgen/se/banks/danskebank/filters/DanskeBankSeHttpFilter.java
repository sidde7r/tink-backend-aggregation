package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.filters;

import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

import javax.ws.rs.core.MediaType;
import java.util.Objects;

public class DanskeBankSeHttpFilter extends Filter {
    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws HttpClientException, HttpResponseException {
        httpRequest.getHeaders().add("Accept", MediaType.APPLICATION_JSON);
        httpRequest.getHeaders().add("x-ibm-client-id", "5ec4b8ad-a93d-43e1-831c-8e78ee6e661a");
        httpRequest.getHeaders().add("x-ibm-client-secret", "O8YSRrLNLwXFYhzZKnuO2PqrDDoRN8d6gsg2VzAiqN49v4jFNU");
        httpRequest.getHeaders().add("x-app-culture", "sv-SE");
        httpRequest.getHeaders().add("x-app-version", "MobileBank ios SE 813854");

        if (Objects.equals(HttpMethod.POST, httpRequest.getMethod())) {
            httpRequest.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON);
        }

        return nextFilter(httpRequest);
    }
}
