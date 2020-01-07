package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.filter;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class HandelsbankenRejectedFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);
        handleResponse(response);
        return response;
    }

    private void handleResponse(HttpResponse response) {
        if (response.getStatus() == HttpStatus.SC_OK) {
            String body = response.getBody(String.class).toLowerCase();
            if (Strings.isNullOrEmpty(body)) {
                return;
            }

            if (body.contains(HandelsbankenBaseConstants.Errors.REQUEST_REJECTED.toLowerCase())
                    && body.contains(
                            HandelsbankenBaseConstants.Errors.REQUEST_REJECTED_MESSAGE
                                    .toLowerCase())) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
        }
    }
}
