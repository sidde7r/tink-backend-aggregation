package se.tink.backend.aggregation.nxgen.http.filter.filters;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
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

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        try {
            return nextFilter(httpRequest);
        } catch (HttpClientException e) {

            if (Strings.isNullOrEmpty(e.getMessage())) {
                throw e;
            }

            BANK_SIDE_FAILURES.stream()
                    .filter((failure -> e.getMessage().toLowerCase().contains(failure)))
                    .findAny()
                    .ifPresent(
                            f -> {
                                throw BankServiceError.BANK_SIDE_FAILURE.exception(e);
                            });

            throw e;
        }
    }
}
