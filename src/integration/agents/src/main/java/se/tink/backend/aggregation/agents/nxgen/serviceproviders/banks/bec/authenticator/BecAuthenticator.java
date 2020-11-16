package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.ScaOptions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.ScaOptionsEncryptedPayload;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.SupplementalFieldsAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.UsernamePasswordAuthenticationStep;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.i18n.Catalog;

@Slf4j
@RequiredArgsConstructor
public class BecAuthenticator extends StatelessProgressiveAuthenticator {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("\\d{10,11}");
    private static final Pattern MOBILECODE_PATTERN = Pattern.compile("\\d{4}");

    private final BecApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final SupplementalRequester supplementalRequester;
    private final PersistentStorage persistentStorage;
    private final Catalog catalog;

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return ImmutableList.of(
                new ScaTokenAuthenticationStep(apiClient, persistentStorage, getDeviceId()),
                new AutomaticAuthenticationStep(this::syncAppDetails, "syncApp"),
                new UsernamePasswordAuthenticationStep(this::fetchScaOptions),
                new DecisionAuthStep(sessionStorage),
                new CombinedNemIdAuthenticationStep(
                        apiClient,
                        supplementalRequester,
                        sessionStorage,
                        persistentStorage,
                        getDeviceId(),
                        catalog),
                new KeyCardAuthenticationStep(sessionStorage, apiClient, getDeviceId()),
                new SupplementalFieldsAuthenticationStep(
                        SupplementalFieldsAuthenticationStep.class.getName(),
                        this::keyCardAuth,
                        prepareKeyCardFields()),
                new FinalKeyCardAuthenticationStep(
                        sessionStorage, persistentStorage, apiClient, getDeviceId()));
    }

    private AuthenticationStepResponse syncAppDetails() {
        apiClient.appSync();
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse fetchScaOptions(String username, String password)
            throws LoginException, NemIdException {
        auditCredentials(username, password);
        ScaOptionsEncryptedPayload payload =
                apiClient.getScaOptions(username, password, getDeviceId());

        if (payload.getSecondFactorOptions().contains(ScaOptions.CODEAPP_OPTION))
            sessionStorage.put(StorageKeys.SCA_OPTION_KEY, ScaOptions.CODEAPP_OPTION);
        else if (payload.getSecondFactorOptions().contains(ScaOptions.KEYCARD_OPTION))
            sessionStorage.put(StorageKeys.SCA_OPTION_KEY, ScaOptions.KEYCARD_OPTION);
        else throw NemIdError.SECOND_FACTOR_NOT_REGISTERED.exception();

        return AuthenticationStepResponse.executeNextStep();
    }

    private void auditCredentials(String username, String password) {
        log.info("Username matches pattern: {} ", USERNAME_PATTERN.matcher(username).matches());
        log.info("Password matches pattern: {} ", MOBILECODE_PATTERN.matcher(password).matches());
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return true;
    }

    private AuthenticationStepResponse keyCardAuth(Map<String, String> callbackData) {
        String challengeResponseValue = callbackData.get(CommonFields.KeyCardCode.getFieldKey());
        sessionStorage.put(StorageKeys.KEY_CARD_CHALLENGE_RESPONSE_KEY, challengeResponseValue);

        return AuthenticationStepResponse.executeStepWithId(FinalKeyCardAuthenticationStep.STEP_ID);
    }

    private Field[] prepareKeyCardFields() {
        Field keyCardInfoField =
                CommonFields.KeyCardInfo.build(
                        catalog,
                        sessionStorage.get(StorageKeys.CHALLENGE_STORAGE_KEY),
                        sessionStorage.get(StorageKeys.KEY_CARD_NUMBER_STORAGE_KEY));
        Field keyCardCodeField = CommonFields.KeyCardCode.build(catalog, 6);

        return new Field[] {keyCardInfoField, keyCardCodeField};
    }

    private String getDeviceId() {
        String deviceId = persistentStorage.get(StorageKeys.DEVICE_ID_STORAGE_KEY);
        if (deviceId != null) {
            return deviceId;
        } else {
            String generatedDeviceId = generateDeviceId();
            persistentStorage.put(StorageKeys.DEVICE_ID_STORAGE_KEY, generatedDeviceId);
            return generatedDeviceId;
        }
    }

    private String generateDeviceId() {
        return UUID.randomUUID().toString();
    }
}
