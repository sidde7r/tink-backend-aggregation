package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@Slf4j
public class CreditAgricoleStatusHandler extends DefaultResponseStatusHandler {

    private void checkInternalServerError(HttpResponse httpResponse) {
        if (httpResponse.getStatus() == 500) {
            log.info(
                    "Response status {} : {}",
                    httpResponse.getStatus(),
                    httpResponse.getBody(String.class));
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        checkInternalServerError(httpResponse);
        super.handleResponse(httpRequest, httpResponse);
    }
}
