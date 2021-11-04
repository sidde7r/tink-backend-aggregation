package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class LaBanquePostaleResponseErrorHandler extends DefaultResponseStatusHandler {

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (httpResponse.getStatus() == 404
                && httpResponse.getBody(String.class).contains("404 File Not Found")) {
            throw LoginError.NO_ACCOUNTS.exception();
        } else if (isBankSideError(httpResponse) || isProxyError(httpResponse)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
        super.handleResponse(httpRequest, httpResponse);
    }

    private boolean isBankSideError(HttpResponse httpResponse) {
        return httpResponse.getStatus() == 500
                && httpResponse.getBody(LaBanquePostaleErrorResponse.class).isBankSideError();
    }

    private boolean isProxyError(HttpResponse httpResponse) {
        return httpResponse.getStatus() == 502
                && httpResponse.getBody(LaBanquePostaleProxyErrorResponse.class).isProxyError();
    }
}
