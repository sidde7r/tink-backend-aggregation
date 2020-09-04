package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class FinancialOrganisationIdFilter extends Filter {

    private final String organisationId;

    public FinancialOrganisationIdFilter(String organisationId) {
        this.organisationId = organisationId;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        httpRequest
                .getHeaders()
                .add(OpenIdConstants.HttpHeaders.X_FAPI_FINANCIAL_ID, organisationId);

        return nextFilter(httpRequest);
    }
}
