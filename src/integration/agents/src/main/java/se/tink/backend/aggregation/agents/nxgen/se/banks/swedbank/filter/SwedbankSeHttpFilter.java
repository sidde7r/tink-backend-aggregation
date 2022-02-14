package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.filter;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants.HeaderValues;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class SwedbankSeHttpFilter extends Filter {

    private final String userAgent;
    private final String psuIpAddress;

    public SwedbankSeHttpFilter(String userAgent, String psuIpAddress) {
        this.userAgent = Preconditions.checkNotNull(userAgent);
        this.psuIpAddress = psuIpAddress;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        httpRequest.getHeaders().add(HeaderKeys.X_CLIENT, userAgent);
        httpRequest.getHeaders().add(HeaderKeys.ADRUM_1, HeaderValues.ADRUM_1);
        httpRequest.getHeaders().add(HeaderKeys.ADRUM, HeaderValues.ADRUM);

        if (isBankIdPolling(httpRequest)) {
            // Send user IP on BankID polling request
            if (com.google.common.base.Strings.isNullOrEmpty(psuIpAddress)) {
                log.warn("Missing PSU IP address");
            } else {
                httpRequest.getHeaders().add(HeaderKeys.X_FORWARDED_FOR, psuIpAddress);
            }
        }

        HttpResponse resp = nextFilter(httpRequest);
        // Don't handle http exceptions when fetching transactions. Since Swedbank frequently
        // returns 500 when fetching transactions we've added logic to return whatever we got
        // in the transaction fetcher.
        if (!httpRequest.getUrl().get().contains(SwedbankSEConstants.Endpoint.TRANSACTIONS_BASE)) {
            handleException(resp);
        }

        return resp;
    }

    private boolean isBankIdPolling(HttpRequest request) {
        return request.getUrl().toString().endsWith("/mobile/verify")
                && request.getMethod() == HttpMethod.GET;
    }

    private void handleException(HttpResponse response) {
        if (response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            String error = Strings.nullToEmpty(response.getBody(String.class)).toLowerCase();
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    "Http status: " + response.getStatus() + ", body: " + error);
        }
    }
}
