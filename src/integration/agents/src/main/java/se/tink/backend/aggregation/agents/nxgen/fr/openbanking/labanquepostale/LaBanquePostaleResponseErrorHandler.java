package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class LaBanquePostaleResponseErrorHandler extends DefaultResponseStatusHandler {

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (isBankSideError(httpResponse)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
        super.handleResponse(httpRequest, httpResponse);
    }

    private boolean isBankSideError(HttpResponse httpResponse) {
        return httpResponse.getBody(LaBanquePostaleErrorResponse.class).isBankSideError();
    }
}
