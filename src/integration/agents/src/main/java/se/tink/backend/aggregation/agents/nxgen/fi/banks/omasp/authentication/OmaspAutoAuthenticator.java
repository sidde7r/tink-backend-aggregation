package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication;

import com.google.common.base.Strings;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.OmaspApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.OmaspConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.OmaspConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.rpc.OmaspErrorResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class OmaspAutoAuthenticator implements AutoAuthenticator {

    private static final AggregationLogger logger =
            new AggregationLogger(OmaspAutoAuthenticator.class);

    private final OmaspApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;
    private SessionStorage sessionStorage;

    public OmaspAutoAuthenticator(
            OmaspApiClient apiClient,
            PersistentStorage persistentStorage,
            Credentials credentials,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);
        String deviceId = persistentStorage.get(Storage.DEVICE_ID);
        String deviceToken = persistentStorage.get(Storage.DEVICE_TOKEN);

        if (Strings.isNullOrEmpty(username)
                || Strings.isNullOrEmpty(password)
                || Strings.isNullOrEmpty(deviceId)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        try {
            LoginResponse loginResponse =
                    apiClient.login(username, password, deviceId, deviceToken);
            if (loginResponse.getSecurityKeyRequired()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            // We get a new device token every login, to be used upon next login
            persistentStorage.put(Storage.DEVICE_TOKEN, loginResponse.getDeviceToken());
            sessionStorage.put(Storage.FULL_NAME, loginResponse.getName());
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() != HttpStatus.SC_UNAUTHORIZED
                    && e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST) {
                throw e;
            }

            OmaspErrorResponse omaspErrorResponse = apiClient.getError(e.getResponse());

            String error = omaspErrorResponse.getError();
            if (error == null) {
                throw e;
            }

            switch (error.toLowerCase()) {
                case OmaspConstants.Error.AUTHENTICATION_FAILED:
                case OmaspConstants.Error.BAD_REQUEST:
                    throw SessionError.SESSION_EXPIRED.exception(e);
                default:
                    logger.warn(
                            String.format(
                                    "%s: Unknown error code for loginRequest: %s, Message: %s",
                                    OmaspConstants.LogTags.LOG_TAG_AUTHENTICATION,
                                    omaspErrorResponse.getError(),
                                    omaspErrorResponse.getMessage()),
                            e);
                    throw e;
            }
        }
    }
}
