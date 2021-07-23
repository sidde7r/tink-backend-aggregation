package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.filter;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.filter.BecBankUnavailableUtil.isBankUnavailableErrorMessage;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.filter.BecBankUnavailableUtil.isBankUnavailableStatus;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class BecBankUnavailableErrorFilter extends Filter {
    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (isBankUnavailableStatus(response.getStatus())
                && isBankUnavailableErrorMessage(response.getBody(String.class))) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }

        return response;
    }
}
