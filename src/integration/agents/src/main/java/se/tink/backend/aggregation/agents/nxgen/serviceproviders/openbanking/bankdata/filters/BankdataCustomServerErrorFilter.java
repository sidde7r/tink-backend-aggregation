package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.filters;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.filters.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class BankdataCustomServerErrorFilter extends Filter {

    public static boolean isServerErrorResponse(HttpResponse response) {
        boolean hasServerErrorBody =
                tryParseErrorResponse(response).map(ErrorResponse::isServerError).orElse(false);
        return response.getStatus() == 400 && hasServerErrorBody;
    }

    private static Optional<ErrorResponse> tryParseErrorResponse(HttpResponse response) {
        if (!response.hasBody()) {
            return Optional.empty();
        }
        try {
            ErrorResponse errorResponse = response.getBody(ErrorResponse.class);
            return Optional.of(errorResponse);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest) {

        HttpResponse response = nextFilter(httpRequest);

        if (isServerErrorResponse(response)) {
            throw BankServiceError.NO_BANK_SERVICE.exception();
        }

        return response;
    }
}
