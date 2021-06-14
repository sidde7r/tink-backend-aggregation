package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BelfiusResponseStatusHandler extends DefaultResponseStatusHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(BelfiusResponseStatusHandler.class);

    private PersistentStorage persistentStorage;

    public BelfiusResponseStatusHandler(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (httpResponse.getStatus() == 403
                && httpResponse.getBody(String.class).contains("no_active_consent")) {
            LOGGER.info("Consent invalid. Force to manual authentication.");
            persistentStorage.clear();
            throw SessionError.SESSION_EXPIRED.exception(
                    "User\\System has deactivated the consent");
        } else if (httpResponse.getStatus() == 500
                && httpResponse.getBody(String.class).contains("internal_server_error")) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
        super.handleResponse(httpRequest, httpResponse);
    }
}
