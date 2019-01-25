package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.OmaspApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.OmaspConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.rpc.OmaspErrorResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Field;

public class OmaspAutoAuthenticator implements AutoAuthenticator {

    private static final AggregationLogger LOGGER = new AggregationLogger(OmaspAutoAuthenticator.class);

    private final OmaspApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;

    public OmaspAutoAuthenticator(OmaspApiClient apiClient, PersistentStorage persistentStorage,
            Credentials credentials) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);
        String deviceId = persistentStorage.get(OmaspConstants.Storage.DEVICE_ID);

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password) || Strings.isNullOrEmpty(deviceId)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        try {
            LoginResponse loginResponse = apiClient.login(username, password, deviceId);
            if (loginResponse.getSecurityKeyRequired()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() != 401) {
                throw e;
            }

            OmaspErrorResponse omaspErrorResponse = apiClient.getError(e.getResponse());

            String error = omaspErrorResponse.getError();
            if (error == null) {
                throw e;
            }

            switch (error.toLowerCase()) {
            case OmaspConstants.Error.AUTHENTICATION_FAILED:
                throw SessionError.SESSION_EXPIRED.exception();
            default:
                LOGGER.warn(String.format("%s: Unknown error code for loginRequest: %s, Message: %s",
                                OmaspConstants.LogTags.LOG_TAG_AUTHENTICATION,
                                omaspErrorResponse.getError(),
                                omaspErrorResponse.getMessage()));
                throw e;
            }
        }
    }
}
