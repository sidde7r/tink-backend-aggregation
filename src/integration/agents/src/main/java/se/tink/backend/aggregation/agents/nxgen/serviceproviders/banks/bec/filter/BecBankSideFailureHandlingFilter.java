package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.filter;

import com.google.common.collect.ImmutableList;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class BecBankSideFailureHandlingFilter extends Filter {

    private static final ImmutableList<String> BANK_SIDE_FAILURES =
            ImmutableList.of("connection reset", "connect timed out", "read timed out");

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        try {
            return nextFilter(httpRequest);
        } catch (HttpClientException e) {
            BANK_SIDE_FAILURES.stream()
                    .filter((f -> f.contains(e.getMessage().toLowerCase())))
                    .findAny()
                    .ifPresent(
                            f -> {
                                throw BankServiceError.BANK_SIDE_FAILURE.exception(e);
                            });

            throw e;
        }
    }
}
