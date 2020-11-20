package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.filter;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.Preconditions;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants.HeaderValues;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SwedbankSeHttpFilter extends Filter {

    private final String userAgent;

    public SwedbankSeHttpFilter(String userAgent) {
        this.userAgent = Preconditions.checkNotNull(userAgent);
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        httpRequest.getHeaders().add(HeaderKeys.X_CLIENT, userAgent);
        httpRequest.getHeaders().add(HeaderKeys.ADRUM_1, HeaderValues.ADRUM_1);
        httpRequest.getHeaders().add(HeaderKeys.ADRUM, HeaderValues.ADRUM);

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
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    "Http status: " + response.getStatus() + ", body: " + error);
        }
    }
}
