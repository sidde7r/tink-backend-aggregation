package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken.filters;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.HeaderKeys.PSU_CORPORATE_ID;

import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Urls;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@NoArgsConstructor
public class AddCorporateFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        if (httpRequest.getUrl().get().contains(Urls.AUTHORIZATION.toString())) {
            httpRequest.getHeaders().add(PSU_CORPORATE_ID, "UNKNOWN");
        }
        return nextFilter(httpRequest);
    }
}
