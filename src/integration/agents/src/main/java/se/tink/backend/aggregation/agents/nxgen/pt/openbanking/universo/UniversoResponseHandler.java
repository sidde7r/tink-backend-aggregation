package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class UniversoResponseHandler extends DefaultResponseStatusHandler {

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (isServiceBlockedResponse(httpResponse)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        } else if (isBackgroundRefreshNumberExceeded(httpResponse)) {
            throw BankServiceError.ACCESS_EXCEEDED.exception();
        }
        super.handleResponse(httpRequest, httpResponse);
    }

    public boolean isServiceBlockedResponse(HttpResponse httpResponse) {
        return httpResponse.getStatus() == 403
                && httpResponse.hasBody()
                && httpResponse.getBody(ErrorResponse.class).getTppMessages() != null
                && httpResponse.getBody(ErrorResponse.class).getTppMessages().stream()
                        .anyMatch(message -> "SERVICE_BLOCKED".equals(message.getCode()));
    }

    public boolean isBackgroundRefreshNumberExceeded(HttpResponse httpResponse) {
        return httpResponse.getStatus() == 429
                && httpResponse.hasBody()
                && httpResponse.getBody(ErrorResponse.class).getTppMessages() != null
                && httpResponse.getBody(ErrorResponse.class).getTppMessages().stream()
                        .anyMatch(message -> "ACCESS_EXCEEDED".equals(message.getCode()));
    }
}
