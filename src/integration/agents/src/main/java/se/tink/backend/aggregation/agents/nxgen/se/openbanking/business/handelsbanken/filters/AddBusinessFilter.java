package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.handelsbanken.filters;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Urls;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class AddBusinessFilter extends Filter {
    private String businessId;

    public AddBusinessFilter(String businessId) {
        this.businessId = businessId;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        if (httpRequest.getUrl().get() == Urls.AUTHORIZATION.toString()) {

            httpRequest.getHeaders().add("PSU-Corporate-ID", businessId);
        }

        return nextFilter(httpRequest);
    }
}
