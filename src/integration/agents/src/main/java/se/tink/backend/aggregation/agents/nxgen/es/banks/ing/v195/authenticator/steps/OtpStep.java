package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.steps;

import com.google.common.base.Strings;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.PutRestSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.rpc.ErrorCodeMessage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class OtpStep extends AbstractAuthenticationStep {

    private static final Logger LOGGER = LoggerFactory.getLogger(OtpStep.class);
    public static final String STEP_ID = "IngOtpStep";
    private final IngApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public OtpStep(
            IngApiClient apiClient,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper) {
        super(STEP_ID);
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        final String code;
        try {
            code = supplementalInformationHelper.waitForOtpInput();
        } catch (SupplementalInfoException e) {
            throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(e);
        }

        PutRestSessionResponse putSessionResponse;
        try {
            putSessionResponse = apiClient.putLoginRestSession(code, getProcessId());
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN) {
                ErrorCodeMessage error = e.getResponse().getBody(ErrorCodeMessage.class);
                if (error.getErrorCode() == ErrorCodes.INCORRECT_SMS_CODE) {
                    throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
                }
            }
            throw e;
        }

        if (Strings.isNullOrEmpty(putSessionResponse.getTicket())) {
            LOGGER.warn("No ticket on response, check error.");
            throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception();
        }

        persistentStorage.put(Storage.CREDENTIALS_TOKEN, putSessionResponse.getRememberMeToken());
        apiClient.postLoginAuthResponse(putSessionResponse.getTicket());
        return AuthenticationStepResponse.authenticationSucceeded();
    }

    private String getProcessId() {
        return sessionStorage.get(Storage.LOGIN_PROCESS_ID);
    }
}
