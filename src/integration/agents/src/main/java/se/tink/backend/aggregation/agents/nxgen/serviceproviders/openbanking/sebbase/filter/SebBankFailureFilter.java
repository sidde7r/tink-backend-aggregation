package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SebBankFailureFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == HttpStatus.SC_BAD_GATEWAY) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    "Http status: " + HttpStatus.SC_BAD_GATEWAY);
        }

        if (response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            final ErrorResponse errorResponse = response.getBody(ErrorResponse.class);
            if (ErrorMessages.SEB_SPECIFIC_ERROR.equalsIgnoreCase(errorResponse.getTitle())) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(
                        String.format(
                                "SEB error %s: %s",
                                errorResponse.getCode(), errorResponse.getDetail()));
            }
        }

        return response;
    }
}
