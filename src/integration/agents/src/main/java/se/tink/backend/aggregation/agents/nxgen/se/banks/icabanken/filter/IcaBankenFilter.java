package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.filter;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.http.HttpHeaders;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

public class IcaBankenFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        MultivaluedMap<String, Object> headers = httpRequest.getHeaders();

        headers.add(IcaBankenConstants.Headers.ACCEPT, MediaType.APPLICATION_JSON);
        headers.add(
                IcaBankenConstants.Headers.HEADER_API_VERSION,
                IcaBankenConstants.Headers.VALUE_API_VERSION);
        headers.add(
                IcaBankenConstants.Headers.HEADER_APIKEY, IcaBankenConstants.Headers.VALUE_APIKEY);
        headers.add(HttpHeaders.USER_AGENT, AbstractAgent.DEFAULT_USER_AGENT);
        headers.add(
                IcaBankenConstants.Headers.HEADER_CLIENTAPPVERSION,
                IcaBankenConstants.Headers.VALUE_CLIENTAPPVERSION);
        headers.add(
                IcaBankenConstants.Headers.HEADER_CLIENT_OS,
                IcaBankenConstants.Headers.VALUE_CLIENT_OS);
        headers.add(
                IcaBankenConstants.Headers.HEADER_CLIENT_OS_VERSION,
                IcaBankenConstants.Headers.VALUE_CLIENT_OS_VERSION);
        headers.add(
                IcaBankenConstants.Headers.HEADER_CLIENT_HARDWARE,
                IcaBankenConstants.Headers.VALUE_CLIENT_HARDWARE);

        return nextFilter(httpRequest);
    }
}
