package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.filter;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
public final class RabobankUserRefreshLimitExceededFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        Optional.of(response)
                .filter(this::accessExceeded)
                .map(this::responseBody)
                .filter(this::shouldThrow)
                .ifPresent(
                        body -> {
                            throw BankServiceError.ACCESS_EXCEEDED.exception(
                                    exceptionMessage(response));
                        });
        return response;
    }

    private boolean accessExceeded(HttpResponse response) {
        return response.getStatus() == 429;
    }

    private String responseBody(HttpResponse response) {
        return response.getBody(String.class);
    }

    private boolean shouldThrow(String responseBody) {
        return responseBody
                .toLowerCase()
                .contains("calls for unattended requests has been exceeded for account with id");
    }

    private static String exceptionMessage(HttpResponse response) {
        return String.format(
                "Http status: %s Error body: %s",
                response.getStatus(), response.getBody(String.class));
    }
}
