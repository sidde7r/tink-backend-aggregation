package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.filter;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants.Errors;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class SwedbankSeHttpFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse resp = nextFilter(httpRequest);

        // Don't handle http exceptions when fetching transactions. Since Swedbank frequently
        // returns 500 when fetching transactions we've added logic to return whatever we got
        // in the transaction fetcher.
        if (!httpRequest.getUrl().get().contains(SwedbankSEConstants.Endpoint.TRANSACTIONS_BASE)) {
            handleException(resp);
        }

        return resp;
    }

    private void handleException(HttpResponse response) {
        if (response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            String error = Strings.nullToEmpty(response.getBody(String.class)).toLowerCase();
            if (error.contains(SwedbankSEConstants.Errors.INTERNAL_SERVER_ERROR.toLowerCase())
                    && error.contains(Errors.INTERNAL_TECHNICAL_ERROR.toLowerCase())) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(
                        "Http status: " + response.getStatus() + ", body: " + error);
            }
        }
    }
}
