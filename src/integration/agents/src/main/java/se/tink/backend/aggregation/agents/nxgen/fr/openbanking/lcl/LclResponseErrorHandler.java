package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class LclResponseErrorHandler extends DefaultResponseStatusHandler {

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (isBankUnderMaintenance(httpResponse)) {
            throw BankServiceError.NO_BANK_SERVICE.exception();
        }
        super.handleResponse(httpRequest, httpResponse);
    }

    private boolean isBankUnderMaintenance(HttpResponse httpResponse) {
        return httpResponse.getStatus() == 503
                && httpResponse
                        .getBody(String.class)
                        .contains("LCL DSP2 APIS are currently under maintenance");
    }
}
