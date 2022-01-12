package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.steps;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.Log.BEC_LOG_TAG;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.agent.sdk.operation.User;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.LoggedInEntity;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BecAutoAuthenticationStep {

    private final BecApiClient apiClient;
    private final Credentials credentials;
    private final BecStorage storage;
    private final User user;

    /**
     * Try to auto authenticate.
     *
     * @return if auto authentication was successful
     */
    public boolean tryAutoAuthentication() {
        if (isSessionInvalid()) {
            if (user.isAvailableForInteraction()) {
                return false;
            }
            throw SessionError.SESSION_EXPIRED.exception();
        }

        try {
            refreshScaToken();
            log.info("{} Auto authentication successful", BEC_LOG_TAG);
            return true;

        } catch (LoginException loginException) {
            storage.clearSessionData();

            if (user.isAvailableForInteraction()) {
                log.error("Refresh SCA token error -> forcing manual auth");
                return false;
            }
            throw loginException;
        }
    }

    private boolean isSessionInvalid() {
        if (storage.getScaToken() == null) {
            log.info("{} Session invalid - missing SCA token", BEC_LOG_TAG);
            return true;
        }
        if (storage.getDeviceId() == null) {
            log.info("{} Session invalid - missing device id", BEC_LOG_TAG);
            return true;
        }
        return false;
    }

    private void refreshScaToken() {
        LoggedInEntity loggedInEntity =
                apiClient.authScaToken(
                        credentials.getField(Key.USERNAME),
                        credentials.getField(Key.PASSWORD),
                        storage.getScaToken(),
                        storage.getDeviceId());
        storage.saveScaToken(loggedInEntity.getScaToken());
    }
}
