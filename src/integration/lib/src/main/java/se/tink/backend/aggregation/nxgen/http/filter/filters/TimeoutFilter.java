package se.tink.backend.aggregation.nxgen.http.filter.filters;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

import com.google.common.collect.ImmutableList;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

/**
 * Utility filter to throw a Tink {@link BankServiceError} when we don't get a response from the
 * bank's API.
 */
public class TimeoutFilter extends Filter {

    private static final ImmutableList<String> BANK_SIDE_FAILURES =
            ImmutableList.of(
                    "connection reset", "connect timed out", "read timed out", "failed to respond");

    public static boolean isConnectionTimeoutException(HttpClientException e) {
        return BANK_SIDE_FAILURES.stream()
                .anyMatch((failure -> containsIgnoreCase(e.getMessage(), failure)));
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest) {
        try {
            return nextFilter(httpRequest);

        } catch (HttpClientException e) {
            if (isConnectionTimeoutException(e)) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(e);
            }
            throw e;
        }
    }
}
