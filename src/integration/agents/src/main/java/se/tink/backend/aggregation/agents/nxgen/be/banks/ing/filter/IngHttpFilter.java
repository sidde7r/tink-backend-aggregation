package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.filter;

import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

public class IngHttpFilter extends Filter {
    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws HttpClientException, HttpResponseException {
        HttpResponse httpResponse = nextFilter(httpRequest);
        int httpStatus = httpResponse.getStatus();
        String responseBody = httpResponse.getBody(String.class);
        boolean isUnavailable = responseBody.toLowerCase().contains("Unavailable");
        if (httpStatus >= 500 && isUnavailable) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
        return httpResponse;
    }
}
