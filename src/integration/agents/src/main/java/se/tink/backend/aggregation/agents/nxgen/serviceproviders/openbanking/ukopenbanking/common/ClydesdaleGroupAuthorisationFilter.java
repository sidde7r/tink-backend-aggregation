package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.UkObErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

/*
   Even though 403 status with UK.CYBG.Forbidden error code suggests that it is client's fault,
   there was a case when CBG group was sending such response due to their internal issues.
   Keep that in mind, when it occurs again.
*/
public class ClydesdaleGroupAuthorisationFilter extends Filter {

    private static final String CODE_ERROR_FORBIDDEN = "UK.CYBG.Forbidden";

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);
        if (is403(response) && hasErrorCodeForbidden(response)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
        return response;
    }

    private boolean is403(HttpResponse response) {
        return response.getStatus() == HttpStatus.SC_FORBIDDEN;
    }

    private boolean hasErrorCodeForbidden(HttpResponse response) {
        return response.getBody(UkObErrorResponse.class).hasErrorCode(CODE_ERROR_FORBIDDEN);
    }
}
