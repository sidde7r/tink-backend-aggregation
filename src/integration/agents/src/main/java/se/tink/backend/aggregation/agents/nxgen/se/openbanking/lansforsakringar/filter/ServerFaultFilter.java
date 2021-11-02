package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.filter;

import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.rpc.FaultResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class ServerFaultFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        try {
            return nextFilter(httpRequest);

        } catch (HttpResponseException e) {
            final HttpResponse response = e.getResponse();

            throwServerFaultError(response);
            throw e;
        }
    }

    private void throwServerFaultError(HttpResponse response) {
        if (response.getStatus() == HttpStatus.SC_UNAUTHORIZED
                && MediaType.APPLICATION_JSON_TYPE.isCompatible(response.getType())) {
            final FaultResponse faultResponse = response.getBody(FaultResponse.class);
            if (faultResponse.isServerFault()) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
        }
    }
}
