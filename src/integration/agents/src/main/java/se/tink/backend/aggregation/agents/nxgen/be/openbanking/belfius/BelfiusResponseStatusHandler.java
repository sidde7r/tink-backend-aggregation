package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
@RequiredArgsConstructor
public class BelfiusResponseStatusHandler extends DefaultResponseStatusHandler {

    private final PersistentStorage persistentStorage;

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (httpResponse.getStatus() == 403
                && httpResponse.getBody(String.class).contains("no_active_consent")) {
            log.info("Consent invalid. Force to manual authentication.");
            persistentStorage.clear();
            throw SessionError.SESSION_EXPIRED.exception(
                    "User\\System has deactivated the consent");
        }
        super.handleResponse(httpRequest, httpResponse);
    }
}
