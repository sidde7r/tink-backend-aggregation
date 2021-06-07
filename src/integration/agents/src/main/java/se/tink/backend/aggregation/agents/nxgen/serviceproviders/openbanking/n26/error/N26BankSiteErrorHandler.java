package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.error;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RequiredArgsConstructor
public class N26BankSiteErrorHandler extends DefaultResponseStatusHandler {

    private final N26BankSiteErrorDiscoverer errorDiscoverer;

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (httpResponse.getStatus() >= 500 && errorDiscoverer.isBankSiteError(httpResponse)) {
            throwBankSiteException(httpResponse.getBody(String.class));
        }
        super.handleResponse(httpRequest, httpResponse);
    }

    private void throwBankSiteException(String message) {
        throw BankServiceError.BANK_SIDE_FAILURE.exception(message);
    }
}
