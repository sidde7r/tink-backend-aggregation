package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants.Authentication;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities.KeyValueEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities.SabadellSessionData;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities.SecurityInputEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities.SessionResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.OtpStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.UsernamePasswordAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SabadellAuthenticator extends StatelessProgressiveAuthenticator {
    private static final Logger LOG = LoggerFactory.getLogger(SabadellAuthenticator.class);
    private final SabadellApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final List<AuthenticationStep> authenticationSteps;
    private final RandomValueGenerator randomValueGenerator;

    public SabadellAuthenticator(
            SabadellApiClient apiClient,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            SupplementalInformationFormer supplementalInformationFormer,
            RandomValueGenerator randomValueGenerator) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.authenticationSteps =
                ImmutableList.of(
                        new UsernamePasswordAuthenticationStep(this::login),
                        new AutomaticAuthenticationStep(this::checkSCA, "checkSCA"),
                        new OtpStep(this::processOtp, supplementalInformationFormer));
        this.randomValueGenerator = randomValueGenerator;
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return authenticationSteps;
    }

    private AuthenticationStepResponse login(String username, String password)
            throws LoginException {
        final String csid = getCSID();
        final SessionResponse response = apiClient.initiateSession(username, password, csid, null);
        SabadellSessionData sessionData = fetchSessionData();
        sessionData.setSessionResponse(response);
        sessionData.setCredentials(username, password);
        storeSessionData(sessionData);
        return AuthenticationStepResponse.executeNextStep();
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

    private AuthenticationStepResponse processOtp(String otp) throws LoginException {
        SabadellSessionData sessionData = fetchSessionData();
        final SessionResponse initResponse = getStoredSessionResponse();
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
                apiClient.initiateSession(
                        sessionData.getUsername(), sessionData.getPassword(), csid, securityInput);
        sessionData.setSessionResponse(response);
        storeSessionData(sessionData);
        return AuthenticationStepResponse.executeNextStep();
    }

    private SessionResponse getStoredSessionResponse() {
        return fetchSessionData().getSessionResponse();
    }

    private void storeSessionData(SabadellSessionData sessionData) {
        sessionStorage.put(Storage.SESSION_KEY, sessionData);
    }

    private SabadellSessionData fetchSessionData() {
        return sessionStorage
                .get(Storage.SESSION_KEY, SabadellSessionData.class)
                .orElse(new SabadellSessionData());
    }

    private String getCSID() {
        if (!persistentStorage.containsKey(Storage.CSID_KEY)) {
            persistentStorage.put(
                    Storage.CSID_KEY, randomValueGenerator.getUUID().toString().toUpperCase());
        }
        return persistentStorage.get(Storage.CSID_KEY);
    }
}
