package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.filter;

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
        final HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == HttpStatus.SC_UNAUTHORIZED && response.hasBody()) {
            final FaultResponse faultResponse = response.getBody(FaultResponse.class);
            if (faultResponse.isServerFault()) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
        }

        return response;
    }
}
