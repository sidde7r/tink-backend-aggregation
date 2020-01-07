package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.filters;

import com.google.common.base.Strings;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants.ErrorCodes;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class NordeaInternalServerErrorFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);
        if (response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR && response.hasBody()) {
            String body = response.getBody(String.class);
            if (!Strings.isNullOrEmpty(body) && isBankSideFailure(body)) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception("Error body: " + body);
            }
        }
        return response;
    }

    private boolean isBankSideFailure(String body) {
        String lowerCaseBody = body.toLowerCase();
        return (lowerCaseBody.contains(ErrorCodes.INTERNAL_SERVER_ERROR)
                        && lowerCaseBody.contains(ErrorCodes.INTERNAL_SERVER_ERROR_MESSAGE))
                || (lowerCaseBody.contains(ErrorCodes.REMOTE_SERVICE_ERROR)
                        && lowerCaseBody.contains(ErrorCodes.REMOTE_SERVICE_ERROR_MESSAGE))
                || lowerCaseBody.contains(ErrorCodes.HYSTRIX_CIRCUIT_SHORT_CIRCUITED)
                || lowerCaseBody.contains(ErrorCodes.TIMEOUT_AFTER_MESSAGE);
    }
}
