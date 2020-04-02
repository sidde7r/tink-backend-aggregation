package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants.Authentication;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities.KeyValueEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities.SecurityInputEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities.SessionResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.OtpStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.UsernamePasswordAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SabadellAuthenticator extends StatelessProgressiveAuthenticator {
    private static final Logger LOG = LoggerFactory.getLogger(SabadellAuthenticator.class);
    private final SabadellApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final List<? extends AuthenticationStep> authenticationSteps;

    public SabadellAuthenticator(
            SabadellApiClient apiClient,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            SupplementalInformationFormer supplementalInformationFormer) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.authenticationSteps =
                ImmutableList.of(
                        new UsernamePasswordAuthenticationStep(this::login),
                        new AutomaticAuthenticationStep(this::checkSCA, "checkSCA"),
                        new OtpStep(this::processOtp, supplementalInformationFormer));
    }

    @Override
    public List<? extends AuthenticationStep> authenticationSteps() {
        return authenticationSteps;
    }

    private void login(String username, String password) throws LoginException {
        final String csid = getCSID();
        final SessionResponse response = apiClient.initiateSession(username, password, csid, null);
        storeSessionResponse(response);
    }

    private AuthenticationStepResponse checkSCA() {
        final SessionResponse initResponse = getStoredSessionResponse();
        if (Authentication.TYPE_SCA.equalsIgnoreCase(
                initResponse.getUser().getAuthenticationType())) {
            LOG.info("SCA: required");
            return AuthenticationStepResponse.executeNextStep();
        } else {
            LOG.info("SCA: not required");
            return AuthenticationStepResponse.authenticationSucceeded();
        }
    }

    private AuthenticationStepResponse processOtp(String otp, Credentials credentials)
            throws LoginException {
        final SessionResponse initResponse = getStoredSessionResponse();
        final String username = credentials.getField(Field.Key.USERNAME);
        final String password = credentials.getField(Field.Key.PASSWORD);
        final String csid = getCSID();
        final String keyboardKey =
                initResponse.getUser().getSecurityOutput().getFloatingKeyboard().getKey();
        final String scaPassword =
                initResponse.getUser().getSecurityOutput().getCardData().stream()
                        .filter(kv -> kv.getKey().equalsIgnoreCase(otp))
                        .findFirst()
                        .map(KeyValueEntity::getValue)
                        .orElseThrow(() -> new IllegalStateException(("Invalid SCA key")));
        final SecurityInputEntity securityInput = SecurityInputEntity.of(keyboardKey, scaPassword);

        LOG.info("SCA: entering OTP");
        final SessionResponse response =
                apiClient.initiateSession(username, password, csid, securityInput);

        storeSessionResponse(response);
        return AuthenticationStepResponse.executeNextStep();
    }

    private SessionResponse getStoredSessionResponse() {
        return sessionStorage
                .get(Storage.SESSION_KEY, SessionResponse.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Init response not found in session storage."));
    }

    private void storeSessionResponse(SessionResponse response) {
        sessionStorage.put(Storage.SESSION_KEY, response);
    }

    private String getCSID() {
        if (!persistentStorage.containsKey(Storage.CSID_KEY)) {
            persistentStorage.put(Storage.CSID_KEY, UUID.randomUUID().toString().toUpperCase());
        }
        return persistentStorage.get(Storage.CSID_KEY);
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return true; // TODO: change once automatic flow is implemented
    }
}
