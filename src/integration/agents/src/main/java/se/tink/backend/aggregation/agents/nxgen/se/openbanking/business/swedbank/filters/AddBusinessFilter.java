package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.swedbank.filters;

import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
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

        if (httpRequest
                .getUrl()
                .get()
                .contains(SwedbankConstants.Urls.AUTHORIZATION_DECOUPLED.toString())) {
            httpRequest.getHeaders().add(Field.Key.CORPORATE_ID.getFieldKey(), businessId);
        }
        return nextFilter(httpRequest);
    }
}
