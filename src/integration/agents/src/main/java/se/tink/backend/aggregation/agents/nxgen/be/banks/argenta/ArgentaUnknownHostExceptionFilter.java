package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta;

import java.net.UnknownHostException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class ArgentaUnknownHostExceptionFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        try {
            return nextFilter(httpRequest);
        } catch (Exception exception) {
            if (exception.getCause() instanceof UnknownHostException) {
                throw BankServiceError.NO_BANK_SERVICE.exception();
            }
            throw exception;
        }
    }
}
