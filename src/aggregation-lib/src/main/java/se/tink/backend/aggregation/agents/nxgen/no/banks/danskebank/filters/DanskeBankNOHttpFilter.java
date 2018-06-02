package se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.filters;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.filters.DanskeBankHttpFilter;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

import javax.ws.rs.core.MediaType;
import java.util.Objects;

public class DanskeBankNOHttpFilter extends DanskeBankHttpFilter {
    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws HttpClientException, HttpResponseException {
        httpRequest.getHeaders().add("Accept", MediaType.APPLICATION_JSON);
        httpRequest.getHeaders().add(CLIENT_ID_KEY, CLIENT_ID_VALUE);
        httpRequest.getHeaders().add(CLIENT_SECRET_KEY, CLIENT_SECRET_VALUE);
        httpRequest.getHeaders().add(APP_CULTURE_KEY, "nb-NO");
        httpRequest.getHeaders().add(APP_VERSION_KEY, "MobileBank ios NO 1022577");

        if (Objects.equals(HttpMethod.POST, httpRequest.getMethod())) {
            httpRequest.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON);
        }

        return nextFilter(httpRequest);
    }
}
