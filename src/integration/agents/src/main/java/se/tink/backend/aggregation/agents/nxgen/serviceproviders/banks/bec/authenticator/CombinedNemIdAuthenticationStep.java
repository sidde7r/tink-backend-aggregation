package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

import java.util.Collections;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.CodeAppTokenEncryptedPayload;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.LoggedInEntity;
import se.tink.backend.aggregation.agents.utils.supplementalfields.DanishFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdPollTimeoutException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.retrypolicy.RetryCallback;
import se.tink.libraries.retrypolicy.RetryExecutor;
import se.tink.libraries.retrypolicy.RetryPolicy;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CombinedNemIdAuthenticationStep implements AuthenticationStep {

    private static final int POLL_NEMID_MAX_ATTEMPTS = 10;

    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final BecApiClient apiClient;
    private final SupplementalRequester supplementalRequester;
    private final RetryExecutor retryExecutor = new RetryExecutor();
    private final String deviceId;
    private final Catalog catalog;

    CombinedNemIdAuthenticationStep(
            BecApiClient apiClient,
            SupplementalRequester supplementalRequester,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            String deviceId,
            Catalog catalog) {
        this.apiClient = apiClient;
        this.supplementalRequester = supplementalRequester;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.deviceId = deviceId;
        this.catalog = catalog;

        retryExecutor.setRetryPolicy(
                new RetryPolicy(POLL_NEMID_MAX_ATTEMPTS, NemIdPollTimeoutException.class));
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        Credentials credentials = request.getCredentials();
        sendNemIdRequest(credentials);
        displayPrompt(credentials);
        pollNemId();
        finalizeAuth(credentials);
        return AuthenticationStepResponse.authenticationSucceeded();
    }

    private void displayPrompt(Credentials credentials) {
        Field field = DanishFields.NemIdInfo.build(catalog);

        credentials.setSupplementalInformation(
                SerializationUtils.serializeToString(Collections.singletonList(field)));
        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);

        supplementalRequester.requestSupplementalInformation(credentials, true);
    }

    private void sendNemIdRequest(final Credentials credentials) throws NemIdException {
        CodeAppTokenEncryptedPayload payload =
                apiClient.getNemIdToken(
                        credentials.getField(Field.Key.USERNAME),
                        credentials.getField(Field.Key.PASSWORD),
                        deviceId);
        sessionStorage.put(
                StorageKeys.TOKEN_STORAGE_KEY, payload.getCodeappTokenDetails().getToken());
    }

    private void pollNemId() throws AuthenticationException {
        retryExecutor.execute(
                (RetryCallback<Void, AuthenticationException>)
                        () -> {
                            apiClient.pollNemId(sessionStorage.get(StorageKeys.TOKEN_STORAGE_KEY));
                            return null;
                        });
    }

    private void finalizeAuth(Credentials credentials) throws ThirdPartyAppException {
        String username = credentials.getField(Key.USERNAME);
        String password = credentials.getField(Key.PASSWORD);
        String token = sessionStorage.get(StorageKeys.TOKEN_STORAGE_KEY);
        LoggedInEntity loggedInEntity = apiClient.authCodeApp(username, password, token, deviceId);
        persistentStorage.put(StorageKeys.SCA_TOKEN_STORAGE_KEY, loggedInEntity.getScaToken());
    }

    @Override
    public String getIdentifier() {
        return getStepIdentifier();
    }

    private static String getStepIdentifier() {
        return CombinedNemIdAuthenticationStep.class.getSimpleName();
    }
}
