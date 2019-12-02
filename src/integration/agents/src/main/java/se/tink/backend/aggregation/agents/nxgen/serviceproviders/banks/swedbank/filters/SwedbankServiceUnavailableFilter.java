package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.filters;

import io.vavr.control.Try;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants.BankErrorMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;

public class SwedbankServiceUnavailableFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
            if (Try.of(() -> response.getBody(ErrorResponse.class))
                    .map(e -> e.hasErrorCode(BankErrorMessage.SERVICE_UNAVAILABLE))
                    .getOrElse(Boolean.FALSE)) {
                throw BankServiceError.NO_BANK_SERVICE.exception();
            }
        }

        return response;
    }
}
