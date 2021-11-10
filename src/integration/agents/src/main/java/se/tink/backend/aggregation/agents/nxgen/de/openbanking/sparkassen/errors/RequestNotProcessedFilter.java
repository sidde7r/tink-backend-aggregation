package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.errors;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class RequestNotProcessedFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest) {
        HttpResponse response = nextFilter(httpRequest);
        if (isRequestNotProcessedError(response)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(response.getBody(String.class));
        }
        return response;
    }

    public static boolean isRequestNotProcessedError(HttpResponse response) {
        return (response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR)
                && ErrorResponse.fromHttpResponse(response)
                        .filter(
                                ErrorResponse.anyTppMessageMatchesPredicate(
                                        SparkassenKnownErrors.REQUEST_PROCESSING_ERROR))
                        .isPresent();
    }
}
