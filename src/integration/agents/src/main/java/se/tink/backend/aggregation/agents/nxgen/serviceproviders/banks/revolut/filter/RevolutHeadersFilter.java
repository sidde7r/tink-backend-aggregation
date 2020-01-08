package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.filter;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class RevolutHeadersFilter extends Filter {

    private void addHeaderIfNotPresent(
            HttpRequest httpRequest, RevolutConstants.AppAuthenticationValues constant) {
        if (!httpRequest.getHeaders().containsKey(constant.getKey())) {
            httpRequest.getHeaders().add(constant.getKey(), constant.getValue());
        }
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        addHeaderIfNotPresent(httpRequest, RevolutConstants.AppAuthenticationValues.API_VERSION);
        addHeaderIfNotPresent(httpRequest, RevolutConstants.AppAuthenticationValues.APP_VERSION);
        addHeaderIfNotPresent(httpRequest, RevolutConstants.AppAuthenticationValues.MODEL);
        addHeaderIfNotPresent(httpRequest, RevolutConstants.AppAuthenticationValues.USER_AGENT);

        return nextFilter(httpRequest);
    }
}
