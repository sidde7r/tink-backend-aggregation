package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.filters;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class DnbRedirectFilter extends Filter {

    private static final String PARTIAL_LOCATION_TECH_ERROR = "tekniskfeil";
    private static final String PARTIAL_LOCATION_MAINTENANCE = "planned_downtime_without_time";

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse httpResponse = nextFilter(httpRequest);
        if (httpResponse.getStatus() == 302) {
            String location = httpResponse.getHeaders().getFirst("Location");
            if (!StringUtils.isEmpty(location)) {
                if (location.contains(PARTIAL_LOCATION_MAINTENANCE)) {
                    throw BankServiceError.NO_BANK_SERVICE.exception();
                }
                if (location.contains(PARTIAL_LOCATION_TECH_ERROR)) {
                    throw BankServiceError.BANK_SIDE_FAILURE.exception();
                }
            }
        }
        return httpResponse;
    }
}
