package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps;

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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.SwedbankBalticsAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class CollectStatusStep implements AuthenticationStep {

    private final SwedbankBalticsAuthenticator authenticator;
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final SwedbankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        // TODO: different checks from old auth
        final Credentials credentials = request.getCredentials();

        final String userId =
                authenticator.verifyCredentialsNotNullOrEmpty(
                        credentials.getField(Field.Key.USERNAME));

        String collectAuthUri = sessionStorage.get(SwedbankBalticsConstants.AUTH_URL);

        for (int i = 0; i < SwedbankBalticsConstants.SMART_ID_POLL_MAX_ATTEMPTS; i++) {

            // TODO: catch exceptions
            AuthenticationStatusResponse authenticationStatusResponse =
                    apiClient.collectAuthStatus(userId, collectAuthUri);
            String status = authenticationStatusResponse.getScaStatus();

            logger.info("response:" + authenticationStatusResponse.getAuthorizationCode());
            logger.info("status:" + status);

            // TODO: switch statuses. Example WaitScaStatusHelper / BankId Controller
            // TODO: handle expiration time and so on

            if (status.equals("finalised")) {
                sessionStorage.put(
                        SwedbankBalticsConstants.AUTH_CODE,
                        authenticationStatusResponse.getAuthorizationCode());
                return AuthenticationStepResponse.executeNextStep();
            }

            Uninterruptibles.sleepUninterruptibly(
                    SwedbankBalticsConstants.SMART_ID_POOL_FREQUENCY, TimeUnit.MILLISECONDS);
        }

        // TODO: here we need to throw exception. it is an ERROR situation!
        return AuthenticationStepResponse.authenticationSucceeded();
    }

    @Override
    public String getIdentifier() {
        return "collect_status_step";
    }
}
