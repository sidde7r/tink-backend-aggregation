package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.filter;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants.Errors;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;

public class SwedbankSeHttpFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse resp = nextFilter(httpRequest);
        handleException(resp);
        return resp;
    }

    private void handleException(HttpResponse response) {
        if (response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            String error = Strings.nullToEmpty(response.getBody(String.class)).toLowerCase();
            if (error.contains(SwedbankSEConstants.Errors.INTERNAL_SERVER_ERROR.toLowerCase())
                    && error.contains(Errors.INTERNAL_TECHNICAL_ERROR.toLowerCase())) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
        }
    }
}
