package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.barclays.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.UkObErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

/**
 * This response can be returned for variety of reasons e.g. account being ineligible (blocked?). In
 * the future Barclays suppose to provide better error granularity
 * https://openbanking.atlassian.net/servicedesk/customer/portal/1/OBSD-25332
 */
public class BarclaysInvalidDataFilter extends Filter {

    private static final String ERROR_CODE = "UK.OBIE.Field.Unexpected";
    private static final String ERROR_MESSAGE =
            "Accounts request could not be processed due to invalid data.";

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (is400(response) && hasSpecificCodeErrorAndMessage(response)) {
            throw BankServiceError.DEFAULT_MESSAGE.exception();
        }

        return response;
    }

    private boolean is400(HttpResponse response) {
        return response.getStatus() == HttpStatus.SC_BAD_REQUEST;
    }

    private boolean hasSpecificCodeErrorAndMessage(HttpResponse response) {
        UkObErrorResponse errorResponse = response.getBody(UkObErrorResponse.class);
        return errorResponse.hasErrorCode(ERROR_CODE)
                && errorResponse.messageContains(ERROR_MESSAGE);
    }
}
