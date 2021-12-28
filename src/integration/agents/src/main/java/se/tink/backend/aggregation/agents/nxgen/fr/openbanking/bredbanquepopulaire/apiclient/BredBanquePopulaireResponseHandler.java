package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.apiclient;

import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class BredBanquePopulaireResponseHandler extends DefaultResponseStatusHandler {

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (isBankInternalError(httpResponse)) {
            handleException(BankServiceError.BANK_SIDE_FAILURE, httpResponse);
        }
        super.handleResponse(httpRequest, httpResponse);
    }

    private boolean isBankInternalError(HttpResponse response) {
        return response.getStatus() >= 500;
    }

    private void handleException(AgentError error, HttpResponse httpResponse) {
        if (httpResponse.hasBody()) {
            throw error.exception(httpResponse.getBody(String.class));
        } else {
            throw error.exception();
        }
    }
}
