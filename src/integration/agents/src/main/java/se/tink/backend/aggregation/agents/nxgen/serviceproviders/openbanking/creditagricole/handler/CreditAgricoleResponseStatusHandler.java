package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.handler;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class CreditAgricoleResponseStatusHandler extends DefaultResponseStatusHandler {
    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (isInternalServerError(httpResponse)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
        super.handleResponse(httpRequest, httpResponse);
    }

    private boolean isInternalServerError(HttpResponse httpResponse) {
        return httpResponse.getStatus() == 500
                && Optional.ofNullable(httpResponse.getBody(String.class))
                        .map(this::checkBodyForInternalServerError)
                        .orElse(false);
    }

    private boolean checkBodyForInternalServerError(String body) {
        return body.contains(INTERNAL_SERVER_ERROR);
    }
}
