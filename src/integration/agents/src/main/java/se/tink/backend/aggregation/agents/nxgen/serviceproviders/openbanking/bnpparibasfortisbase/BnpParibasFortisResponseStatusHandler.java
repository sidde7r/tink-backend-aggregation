package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.BnpParibasFortisBaseConstants.FormValues;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@Slf4j
public class BnpParibasFortisResponseStatusHandler extends DefaultResponseStatusHandler {

    private void checkAccessNotAllowedError(HttpResponse httpResponse) {
        if (httpResponse.getStatus() == 400
                && httpResponse.getBody(String.class).contains(FormValues.ACCESS_NOT_ALLOWED)) {
            log.info(
                    "Response status {} : {}",
                    httpResponse.getStatus(),
                    httpResponse.getBody(String.class));
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        checkAccessNotAllowedError(httpResponse);
        super.handleResponse(httpRequest, httpResponse);
    }
}
