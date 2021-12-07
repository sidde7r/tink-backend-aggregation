package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class SocieteGeneraleResponseHandler extends DefaultResponseStatusHandler {

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (isNoAccessError(httpResponse)) {
            throw LoginError.NO_ACCOUNTS.exception();
        }

        if (httpResponse.getStatus() >= 500) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(httpResponse.getBody(String.class));
        }

        super.handleResponse(httpRequest, httpResponse);
    }

    private boolean isNoAccessError(HttpResponse response) {
        return response.getStatus() == 403
                && response.getBody(ErrorResponse.class).isNoAccessToMobileBanking();
    }
}
