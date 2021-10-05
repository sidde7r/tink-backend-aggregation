package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class BpceResponseHandler extends DefaultResponseStatusHandler {

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (isBankInternalError(httpResponse)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        } else if (isNoAvailableAccountsError(httpResponse)) {
            throw LoginError.NO_ACCOUNTS.exception();
        }
        super.handleResponse(httpRequest, httpResponse);
    }

    private boolean isNoAvailableAccountsError(HttpResponse response) {
        return response.getStatus() == 404
                && response.getBody(BpceErrorResponse.class).isNoAvailableAccounts();
    }

    private boolean isBankInternalError(HttpResponse response) {
        return response.getStatus() == 500
                && response.getBody(BpceErrorResponse.class).isInternalError();
    }
}
