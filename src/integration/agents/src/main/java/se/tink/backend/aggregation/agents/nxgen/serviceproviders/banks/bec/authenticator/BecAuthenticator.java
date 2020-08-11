package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.ScaOptions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.LoggedInEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.ScaOptionsEncryptedPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.error.NemIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.SingleSupplementalFieldAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.UsernamePasswordAuthenticationStep;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BecAuthenticator extends StatelessProgressiveAuthenticator {
    private static final Logger log = LoggerFactory.getLogger(BecAuthenticator.class);

    static final String USERNAME_STORAGE_KEY = "username";
    static final String PASSWORD_STORAGE_KEY = "password";

    private static final Pattern USERNAME_PATTERN = Pattern.compile("\\d{10,11}");
    private static final Pattern MOBILECODE_PATTERN = Pattern.compile("\\d{4}");

    private final BecApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final SupplementalRequester supplementalRequester;
    private final PersistentStorage persistentStorage;

    public BecAuthenticator(
            BecApiClient apiClient,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            SupplementalRequester supplementalRequester) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.supplementalRequester = supplementalRequester;
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return ImmutableList.of(
                new AutomaticAuthenticationStep(this::syncAppDetails, "syncApp"),
                new UsernamePasswordAuthenticationStep(this::fetchScaOptions),
                new DecisionAuthStep(sessionStorage),
                new CombinedNemIdAuthenticationStep(
                        sessionStorage, apiClient, supplementalRequester),
                new KeyCardAuthenticationStep(sessionStorage, persistentStorage, apiClient),
                new SingleSupplementalFieldAuthenticationStep(
                        SingleSupplementalFieldAuthenticationStep.class.getName(),
                        this::keyCardAuth,
                        prepareKeyCardField()));
    }

    private AuthenticationStepResponse syncAppDetails() {
        apiClient.appSync();
        return AuthenticationStepResponse.executeNextStep();
    }

    private void fetchScaOptions(String username, String password)
            throws LoginException, NemIdException {
        auditCredentials(username, password);
        ScaOptionsEncryptedPayload payload = apiClient.scaPrepare(username, password);
        sessionStorage.put(USERNAME_STORAGE_KEY, username);
        sessionStorage.put(PASSWORD_STORAGE_KEY, password);

        if (payload.getSecondFactorOptions().contains(ScaOptions.CODEAPP_OPTION))
            sessionStorage.put(ScaOptions.SCA_OPTION_KEY, ScaOptions.CODEAPP_OPTION);
        else if (payload.getSecondFactorOptions().contains(ScaOptions.KEYCARD_OPTION))
            sessionStorage.put(ScaOptions.SCA_OPTION_KEY, ScaOptions.KEYCARD_OPTION);
        else throw NemIdError.SECOND_FACTOR_NOT_REGISTERED.exception();
    }

    private void auditCredentials(String username, String password) {
        log.info("Username matches pattern: {} ", USERNAME_PATTERN.matcher(username).matches());
        log.info("Password matches pattern: {} ", MOBILECODE_PATTERN.matcher(password).matches());
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return true;
    }

    private AuthenticationStepResponse keyCardAuth(String challengeResponseValue) {
        String nemidChallenge = sessionStorage.get(StorageKeys.CHALLENGE_STORAGE_KEY);
        String userName = sessionStorage.get(USERNAME_STORAGE_KEY);
        String password = sessionStorage.get(PASSWORD_STORAGE_KEY);

        LoggedInEntity loggedInEntity =
                apiClient.postKeyCardChallengeAndDecryptResponse(
                        userName,
                        password,
                        challengeResponseValue,
                        nemidChallenge,
                        persistentStorage.get(StorageKeys.DEVICE_ID_STORAGE_KEY));
        persistentStorage.put(StorageKeys.SCA_TOKEN_STORAGE_KEY, loggedInEntity.getScaToken());

        return AuthenticationStepResponse.authenticationSucceeded();
    }

    private Field prepareKeyCardField() {
        String message =
                String.format(
                        "Please enter code %s from key card number %s",
                        sessionStorage.get(StorageKeys.CHALLENGE_STORAGE_KEY),
                        sessionStorage.get(StorageKeys.KEY_CARD_NUMBER_STORAGE_KEY));
        return Field.builder()
                .immutable(false)
                .description(message)
                .name("challengeValue")
                .numeric(true)
                .minLength(6)
                .maxLength(6)
                .hint("NNNNNN")
                .pattern("([0-9]{4})")
                .build();
    }
}
