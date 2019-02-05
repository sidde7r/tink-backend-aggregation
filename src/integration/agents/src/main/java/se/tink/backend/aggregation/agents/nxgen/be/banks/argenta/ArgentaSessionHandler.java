package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class ArgentaSessionHandler implements SessionHandler {
    private final ArgentaApiClient apiClient;
    private final ArgentaPersistentStorage argentaPersistentStorage;

    public ArgentaSessionHandler(
            ArgentaApiClient apiClient, ArgentaPersistentStorage argentaPersistentStorage) {
        this.apiClient = apiClient;
        this.argentaPersistentStorage = argentaPersistentStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        try {
            String deviceId = argentaPersistentStorage.getDeviceId();
            if (Strings.isNullOrEmpty(deviceId)) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
            apiClient.keepAlive(deviceId);
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
