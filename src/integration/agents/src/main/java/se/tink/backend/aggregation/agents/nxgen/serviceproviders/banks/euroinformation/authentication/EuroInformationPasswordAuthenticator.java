package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.session.rpc.PfmInitResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EuroInformationPasswordAuthenticator implements PasswordAuthenticator {

    public static final String PFM_ENABLED = "pfm_enabled";
    private final Logger LOGGER =
            LoggerFactory.getLogger(EuroInformationPasswordAuthenticator.class);
    private final EuroInformationApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final EuroInformationConfiguration config;

    private EuroInformationPasswordAuthenticator(
            EuroInformationApiClient apiClient,
            SessionStorage sessionStorage,
            EuroInformationConfiguration config) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.config = config;
    }

    public static EuroInformationPasswordAuthenticator create(
            EuroInformationApiClient apiClient,
            SessionStorage sessionStorage,
            EuroInformationConfiguration config) {
        return new EuroInformationPasswordAuthenticator(apiClient, sessionStorage, config);
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        LoginResponse logon = apiClient.logon(username, password);
        if (!EuroInformationUtils.isSuccess(logon.getReturnCode())) {
            handleError(logon);
        }

        sessionStorage.put(EuroInformationConstants.Storage.LOGIN_RESPONSE, logon);
        config.getInitEndpoint()
                .ifPresent(
                        endpoint -> {
                            PfmInitResponse pfmInitResponse = apiClient.actionInit(endpoint);
                            Optional.ofNullable(pfmInitResponse)
                                    .filter(m -> EuroInformationUtils.isSuccess(m.getReturnCode()))
                                    .map(
                                            v -> {
                                                sessionStorage.put(
                                                        EuroInformationConstants.Tags.PFM_ENABLED,
                                                        true);
                                                return v;
                                            })
                                    .orElseGet(
                                            () -> {
                                                LOGGER.info(
                                                        "PFM initialization error: "
                                                                + SerializationUtils
                                                                        .serializeToString(
                                                                                pfmInitResponse));
                                                return null;
                                            });
                        });
    }

    public void handleError(LoginResponse logon) throws SessionException, LoginException {
        EuroInformationErrorCodes errorCode =
                EuroInformationErrorCodes.getByCodeNumber(logon.getReturnCode());
        switch (errorCode) {
            case NOT_LOGGED_IN:
                throw SessionError.SESSION_EXPIRED.exception();
            case LOGIN_ERROR:
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            case TECHNICAL_PROBLEM:
                throw new IllegalStateException(
                        EuroInformationErrorCodes.TECHNICAL_PROBLEM.getCodeNumber());
            case NO_ENUM_VALUE:
                throw new IllegalArgumentException(
                        "Unknown bank value code " + SerializationUtils.serializeToString(logon));
        }
    }
}
