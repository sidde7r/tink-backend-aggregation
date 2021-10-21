package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SibsBadRequestErrorFilter extends Filter {

    private static final int BAD_REQUEST_STATUS = 400;
    private static final String BAD_REQUEST_CODE = "BAD_REQUEST";

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == BAD_REQUEST_STATUS
                && response.hasBody()
                && (response.getBody(String.class).contains(BAD_REQUEST_CODE))) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    String.format(
                            "Http status: %1$s Error body: %2$s",
                            response.getStatus(), response.getBody(String.class)));
        }

        return response;
    }
}
