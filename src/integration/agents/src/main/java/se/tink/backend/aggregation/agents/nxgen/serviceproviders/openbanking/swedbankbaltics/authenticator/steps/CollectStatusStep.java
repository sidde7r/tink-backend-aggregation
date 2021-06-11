package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class CollectStatusStep implements AuthenticationStep {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final SwedbankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        // TODO: chekings

        final Credentials credentials = request.getCredentials();

        String userId = "";
        if (credentials.hasField(Field.Key.USERNAME)) {
            userId = credentials.getField(Field.Key.USERNAME);
            if (Strings.isNullOrEmpty(userId)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
        }

        String collectAuthUri = sessionStorage.get("URL");

        AuthenticationStatusResponse authenticationStatusResponse;
        // TODO try and switch with status

        for (int i = 0; i < 2000; i++) {
            // status = authenticator.collect(reference);
            // todo
            authenticationStatusResponse = apiClient.collectAuthStatus(userId, collectAuthUri);
            String status = authenticationStatusResponse.getScaStatus();

            logger.info("response:" + authenticationStatusResponse.getAuthorizationCode());
            logger.info("status:" + status);
            if (status.equals("finalised")) {
                /*accessToken =
                apiClient.exchangeCodeForToken(
                    authenticationStatusResponse.getAuthorizationCode());*/
                sessionStorage.put(
                        "authorizationCode", authenticationStatusResponse.getAuthorizationCode());
                return AuthenticationStepResponse.executeNextStep();
            }
            Uninterruptibles.sleepUninterruptibly(
                    /*bankIdPollFrequency*/ 90, TimeUnit.MILLISECONDS);
        }

        return AuthenticationStepResponse.authenticationSucceeded();
    }

    @Override
    public String getIdentifier() {
        return "collect_status_step";
    }
}
