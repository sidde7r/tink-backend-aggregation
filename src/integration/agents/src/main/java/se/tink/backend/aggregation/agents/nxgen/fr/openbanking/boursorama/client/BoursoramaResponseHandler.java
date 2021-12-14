package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@Slf4j
public class BoursoramaResponseHandler extends DefaultResponseStatusHandler {

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (isBankSideError(httpResponse)) {
            log.error(
                    "[BoursoramaResponseHandler] Bank side error happened with status: {} and body: {}",
                    httpResponse.getStatus(),
                    httpResponse.getBody(String.class));
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
        super.handleResponse(httpRequest, httpResponse);
    }

    private boolean isBankSideError(HttpResponse httpResponse) {
        return httpResponse.getStatus() >= 500;
    }
}
