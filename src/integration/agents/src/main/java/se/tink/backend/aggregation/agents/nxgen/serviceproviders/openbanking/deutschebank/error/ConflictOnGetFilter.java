package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.error;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class ConflictOnGetFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest) {
        HttpResponse response = nextFilter(httpRequest);
        if (isConflictOnGet(response)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    "Bank issue, returns response code 409 when it shouldn't!");
        }
        return response;
    }

    public static boolean isConflictOnGet(HttpResponse response) {
        return response.getStatus() == 409 && response.getRequest().getMethod() == HttpMethod.GET;
    }
}
