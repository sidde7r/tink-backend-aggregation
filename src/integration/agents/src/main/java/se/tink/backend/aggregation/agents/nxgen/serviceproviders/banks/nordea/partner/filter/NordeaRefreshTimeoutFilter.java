package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.filter;

import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants.HttpFilters;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

/**
 * This filter will throw a BANK_SIDE_FAILURE exception when a request is made more than 10 minutes
 * after the filter is created. This is to avoid long-running refreshes that can be killed without
 * reaching an error state (because the refresh takes longer than the aggregation shutdown time),
 * and later set to AUTHENTICATION_ERROR by the resetHangingCredentials cron job.
 */
public class NordeaRefreshTimeoutFilter extends Filter {
    private Date timeLimit;

    public NordeaRefreshTimeoutFilter() {
        this.timeLimit = DateUtils.addSeconds(new Date(), HttpFilters.REFRESH_TIMEOUT_SECONDS);
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        if (timeLimit.before(new Date())) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    "Refresh took too long - timing out");
        }
        return nextFilter(httpRequest);
    }
}
