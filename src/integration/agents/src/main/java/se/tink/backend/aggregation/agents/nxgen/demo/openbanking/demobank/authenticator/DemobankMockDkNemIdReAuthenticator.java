package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities.NemIdChallengeEntity;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities.NemIdEnrollEntity;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities.NemIdGenerateCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities.NemIdGenerateCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities.NemIdInstallIdEntity;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities.NemIdLoginEncryptionEntity;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities.NemIdLoginInstallIdEncryptionEntity;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities.NemIdLoginWithInstallIdResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppPollResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class DemobankMockDkNemIdReAuthenticator
        extends NemIdCodeAppAuthenticator<NemIdGenerateCodeResponse> implements AutoAuthenticator {

    private final DemobankApiClient apiClient;
    private final PersistentStorage persistentStorage;

    private static final String PSK_USERID = "userId";
    private static final String PSK_PINCODE = "pincode";

    private static final String PSK_INSTALL_ID = "installid";
    private static final String PSK_TOKEN = "token";
    private static final String PSK_CHALLENGE_ENTITY = "challengeEntity";
    private static final String PSK_LOGIN_ENTITY = "loginEntity";

    public DemobankMockDkNemIdReAuthenticator(
            DemobankApiClient apiClient,
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            String userId,
            String pincode) {
        super(client);
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.persistentStorage.put(PSK_USERID, userId);
        this.persistentStorage.put(PSK_PINCODE, pincode);
    }

    @Override
    protected NemIdGenerateCodeResponse initiateAuthentication() {
        String token = UUIDUtils.generateUUID();
        persistentStorage.put(PSK_TOKEN, token);

        NemIdLoginEncryptionEntity encryptionEntity =
                new NemIdLoginEncryptionEntity(
                        persistentStorage.get(PSK_USERID), persistentStorage.get(PSK_PINCODE));

        NemIdChallengeEntity challengeResponse =
                apiClient.nemIdGetChallenge(encryptionEntity, token);
        persistentStorage.put(
                PSK_CHALLENGE_ENTITY, SerializationUtils.serializeToString(challengeResponse));

        return apiClient.nemIdGenerateCode(
                new NemIdGenerateCodeRequest().setPushEnabled(true), token);
    }

    @Override
    protected String getPollUrl(NemIdGenerateCodeResponse initiationResponse) {
        return initiationResponse.getPollUrl();
    }

    @Override
    protected String getInitialReference(NemIdGenerateCodeResponse initiationResponse) {
        return initiationResponse.getToken();
    }

    @Override
    protected void finalizeAuthentication() {
        String pincode = persistentStorage.get(PSK_PINCODE);
        String userId = persistentStorage.get(PSK_USERID);
        String token = persistentStorage.get(PSK_TOKEN);
        if (token == null) {
            throw new IllegalStateException("Can not find token!");
        }

        NemIdCodeAppPollResponse response =
                this.getPollResponse()
                        .orElseThrow(() -> new IllegalStateException("Response is empty!"));

        NemIdEnrollEntity entity =
                new NemIdEnrollEntity(
                        response,
                        pincode,
                        SerializationUtils.deserializeFromString(
                                        persistentStorage.get(PSK_CHALLENGE_ENTITY),
                                        NemIdChallengeEntity.class)
                                .getSecurityDevice());

        NemIdInstallIdEntity installIdEntity = apiClient.nemIdEnroll(entity, token);

        NemIdLoginInstallIdEncryptionEntity encryptionEntity =
                new NemIdLoginInstallIdEncryptionEntity(
                        userId, pincode, installIdEntity.getInstallId());

        persistentStorage.put(PSK_INSTALL_ID, encryptionEntity.getInstallId());

        NemIdLoginWithInstallIdResponse installIdResponse =
                apiClient.nemIdLoginWithInstallId(encryptionEntity, token);

        persistentStorage.put(
                PSK_LOGIN_ENTITY, SerializationUtils.serializeToString(encryptionEntity));

        apiClient.setTokenToStorage(
                OAuth2Token.createBearer(
                        installIdResponse.getSessionToken(),
                        installIdResponse.getSessionToken(),
                        3600));
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, BankServiceException, AuthorizationException {

        if (!persistentStorage.containsKey(PSK_INSTALL_ID)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        String token = persistentStorage.get(PSK_TOKEN);
        if (token == null) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        String userId = persistentStorage.get(PSK_USERID);
        String pinCode = persistentStorage.get(PSK_PINCODE);
        String installId = persistentStorage.get(PSK_INSTALL_ID);
        NemIdLoginInstallIdEncryptionEntity installIdResponse =
                new NemIdLoginInstallIdEncryptionEntity(userId, pinCode, installId);

        NemIdLoginWithInstallIdResponse login =
                apiClient.nemIdLoginWithInstallId(installIdResponse, token);

        apiClient.setTokenToStorage(
                OAuth2Token.createBearer(login.getSessionToken(), login.getSessionToken(), 3600));
    }
}
