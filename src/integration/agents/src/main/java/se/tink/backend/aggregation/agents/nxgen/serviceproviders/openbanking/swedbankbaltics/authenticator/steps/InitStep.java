package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps;

import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class InitStep implements AuthenticationStep {

    private final SwedbankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        final Credentials credentials = request.getCredentials();

        String userId = "";
        if (credentials.hasField(Field.Key.USERNAME)) {
            userId = credentials.getField(Field.Key.USERNAME);
            if (Strings.isNullOrEmpty(userId)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
        }

        String personalId = "";
        if (credentials.hasField(Key.NATIONAL_ID_NUMBER)) {
            personalId = credentials.getField(Field.Key.NATIONAL_ID_NUMBER);
            if (Strings.isNullOrEmpty(personalId)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
        }

        AuthenticationResponse authenticationResponse =
                apiClient.authenticateDecoupled(userId, "EE", personalId);

        String collectUrl = authenticationResponse.getCollectAuthUri();
        sessionStorage.put("URL", collectUrl);

        return AuthenticationStepResponse.executeNextStep();
    }

    @Override
    public String getIdentifier() {
        return "init_step";
    }
}
