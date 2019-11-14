package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.session;

import com.google.api.client.http.HttpStatusCodes;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngHelper;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;

public class IngSessionHandler implements SessionHandler {
    private static final Logger log = LoggerFactory.getLogger(IngSessionHandler.class);
    private final IngApiClient apiClient;
    private final IngHelper ingHelper;

    public IngSessionHandler(IngApiClient apiClient, IngHelper ingHelper) {
        this.apiClient = apiClient;
        this.ingHelper = ingHelper;
    }

    @Override
    public void logout() {
        String logoutUrl = ingHelper.getUrl(IngConstants.RequestNames.LOGOUT);
        apiClient.logout(logoutUrl);
    }

    @Override
    public void keepAlive() throws SessionException {
        long start = System.nanoTime();
        HttpResponse response = apiClient.getMenuItems();
        long total = System.nanoTime() - start;
        long keepAliveRequestTime = TimeUnit.SECONDS.convert(total, TimeUnit.NANOSECONDS);
        log.info("Time getting menu items: {} s", keepAliveRequestTime);

        if (response.getStatus() != HttpStatusCodes.STATUS_CODE_OK) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
