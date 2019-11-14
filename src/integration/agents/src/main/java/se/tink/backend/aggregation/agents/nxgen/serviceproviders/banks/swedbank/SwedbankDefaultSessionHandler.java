package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank;

import java.util.concurrent.TimeUnit;
import org.assertj.core.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.TouchResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class SwedbankDefaultSessionHandler implements SessionHandler {
    private static final Logger log = LoggerFactory.getLogger(SwedbankDefaultSessionHandler.class);

    private final SwedbankDefaultApiClient apiClient;

    public SwedbankDefaultSessionHandler(SwedbankDefaultApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        if (!apiClient.logout()) {
            log.warn("Logout failed");
        }
    }

    @Override
    public void keepAlive() throws SessionException {
        long start = System.nanoTime();
        try {
            TouchResponse response = apiClient.touch();
            long total = System.nanoTime() - start;
            long requestTime = TimeUnit.SECONDS.convert(total, TimeUnit.NANOSECONDS);
            log.info("Time for keepAlive: {} s", requestTime);
            if (response != null
                    && !Strings.isNullOrEmpty(response.getBankId())
                    && !Strings.isNullOrEmpty(response.getChosenProfile())) {
                return;
            }
        } catch (Exception e) {
            long total = System.nanoTime() - start;
            long requestTime = TimeUnit.SECONDS.convert(total, TimeUnit.NANOSECONDS);
            log.info("Time for keepAlive on exception: {} s", requestTime);
            log.warn("Keep alive call (touch) failed: {}", e.getMessage(), e);
        }

        throw SessionError.SESSION_EXPIRED.exception();
    }
}
