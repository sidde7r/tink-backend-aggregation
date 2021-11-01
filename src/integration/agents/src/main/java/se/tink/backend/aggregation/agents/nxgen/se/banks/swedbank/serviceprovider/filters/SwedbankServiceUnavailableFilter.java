package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.filters;

import io.vavr.control.Try;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.BankErrorMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SwedbankServiceUnavailableFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
            // Swedbank return the content type in as
            // Content-Type: : application/json;charset=UTF-8
            // The additional ":" makes parsing the response fail
            addContentTypeHeader(response);

            if (isBankServiceUnavailable(response)) {
                throw BankServiceError.NO_BANK_SERVICE.exception();
            }
        }

        return response;
    }

    private Boolean isBankServiceUnavailable(HttpResponse response) {
        return Try.of(() -> response.getBody(ErrorResponse.class))
                .map(e -> e.hasErrorCode(BankErrorMessage.SERVICE_UNAVAILABLE))
                .getOrElse(Boolean.FALSE);
    }

    private void addContentTypeHeader(HttpResponse response) {
        response.getHeaders().remove(HttpHeaders.CONTENT_TYPE);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    }
}
