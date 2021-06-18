package se.tink.backend.aggregation.nxgen.http.filter.filters;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

/** Filter that will throw {@link BankServiceError} on any 5xx response */
public class ServerErrorFilter extends Filter {

    public static boolean isServerErrorResponse(HttpResponse response) {
        return response.getStatus() >= 500 && response.getStatus() <= 599;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest) {
        HttpResponse response = nextFilter(httpRequest);

        if (isServerErrorResponse(response)) {
            throw BankServiceError.NO_BANK_SERVICE.exception(
                    "Http status: " + response.getStatus());
        }

        return response;
    }
}
