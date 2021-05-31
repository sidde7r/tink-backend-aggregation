package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.filters;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class IngBaseSignatureInvalidFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);
        if (response.getStatus() == HttpStatus.SC_UNAUTHORIZED
                && response.getBody(String.class)
                        .contains(IngBaseConstants.ErrorMessages.INVALID_SIGNATURE)) {
            log.warn("Signature verify error");
            throw BankServiceError.BANK_SIDE_FAILURE.exception("Signature verify error");
        }
        return response;
    }
}
