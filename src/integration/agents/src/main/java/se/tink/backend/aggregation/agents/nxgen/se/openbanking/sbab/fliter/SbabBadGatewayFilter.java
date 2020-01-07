package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fliter;

import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class SbabBadGatewayFilter extends Filter {
    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse httpResponse = nextFilter(httpRequest);
        if (httpResponse.getStatus() == HttpStatus.SC_BAD_GATEWAY) {
            if (MediaType.APPLICATION_JSON_TYPE.equals(httpResponse.getType())
                    && httpResponse.getBody(ErrorResponse.class).isProxyError()) {
                // throw http response exception if the error is an eidas proxy error
                return httpResponse;
            }
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
        return httpResponse;
    }
}
