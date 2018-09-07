package se.tink.backend.aggregation.agents.nxgen.fi.banks.danskebank.filters;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.filters.DanskeBankHttpFilter;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

import javax.ws.rs.core.MediaType;
import java.util.Objects;

public class DanskeBankFIHttpFilter extends DanskeBankHttpFilter {
    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws HttpClientException, HttpResponseException {
        httpRequest.getHeaders().add("Accept", MediaType.APPLICATION_JSON);
        httpRequest.getHeaders().add(CLIENT_ID_KEY, CLIENT_ID_VALUE);
        httpRequest.getHeaders().add(CLIENT_SECRET_KEY, CLIENT_SECRET_VALUE);
        httpRequest.getHeaders().add(ADRUM_KEY, ADRUM_VALUE);
        httpRequest.getHeaders().add(ADRUM1_KEY, ADRUM1_VALUE);
        httpRequest.getHeaders().add(APP_VERSION_KEY, "MobileBank ios FI 1023385");

        if (Objects.equals(HttpMethod.POST, httpRequest.getMethod())) {
            httpRequest.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON);
        }

        return nextFilter(httpRequest);
    }
}
