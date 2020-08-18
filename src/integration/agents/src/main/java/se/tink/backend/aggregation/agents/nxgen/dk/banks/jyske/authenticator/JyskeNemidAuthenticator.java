package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskePersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdChallengeEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdEnrollEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdGenerateCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdGenerateCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdInstallIdEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdLoginEncryptionEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdLoginInstallIdEncryptionEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdLoginWithInstallIdResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Token;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppPollResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.ForceAuthentication;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class JyskeNemidAuthenticator extends NemIdCodeAppAuthenticator<NemIdGenerateCodeResponse>
        implements AutoAuthenticator {

    private final JyskeApiClient apiClient;
    private final JyskePersistentStorage jyskePersistentStorage;

    public JyskeNemidAuthenticator(
            JyskeApiClient apiClient,
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            String userId,
            String pincode,
            CredentialsRequest request) {
        super(client);
        this.apiClient = apiClient;
        this.jyskePersistentStorage = new JyskePersistentStorage(persistentStorage);
        jyskePersistentStorage.setUserId(userId);
        jyskePersistentStorage.setPincode(pincode);

        if (ForceAuthentication.shouldForceAuthentication(request)) {
            jyskePersistentStorage.invalidateToken();
        }
    }

    @Override
    protected NemIdGenerateCodeResponse initiateAuthentication() {
        Token token = jyskePersistentStorage.generateToken();

        apiClient.nemIdInit(token);

        NemIdLoginEncryptionEntity encryptionEntity =
                new NemIdLoginEncryptionEntity(
                        jyskePersistentStorage.getUserId(), jyskePersistentStorage.getPincode());

        NemIdChallengeEntity challengeResponse =
                apiClient
                        .nemIdGetChallenge(encryptionEntity, token)
                        .decrypt(token, NemIdChallengeEntity.class);
        jyskePersistentStorage.setChallengeEntity(challengeResponse);

        NemIdGenerateCodeResponse response =
                apiClient
                        .generateCode(new NemIdGenerateCodeRequest().setPushEnabled(true), token)
                        .decrypt(token, NemIdGenerateCodeResponse.class);

        return response;
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
        String pincode = jyskePersistentStorage.getPincode();
        String userId = jyskePersistentStorage.getUserId();
        Token token =
                jyskePersistentStorage
                        .getToken()
                        .orElseThrow(() -> new IllegalStateException("Can not find token!"));

        NemIdCodeAppPollResponse response =
                this.getPollResponse()
                        .orElseThrow(() -> new IllegalStateException("Response is empty!"));

        NemIdEnrollEntity entity =
                new NemIdEnrollEntity(
                        response,
                        pincode,
                        jyskePersistentStorage.getChallengeEntity().getSecurityDevice());

        NemIdInstallIdEntity installIdEntity =
                apiClient.nemIdEnroll(entity, token).decrypt(token, NemIdInstallIdEntity.class);

        NemIdLoginInstallIdEncryptionEntity encryptionEntity =
                new NemIdLoginInstallIdEncryptionEntity(
                        userId, pincode, installIdEntity.getInstallId());

        jyskePersistentStorage.setInstallId(encryptionEntity.getInstallId());

        NemIdLoginWithInstallIdResponse installIdResponse =
                apiClient
                        .nemIdLoginWithInstallId(encryptionEntity, token)
                        .decrypt(token, NemIdLoginWithInstallIdResponse.class);

        jyskePersistentStorage.setNemidLoginEntity(encryptionEntity);

        apiClient.sendTransportKey(token);
        apiClient.mobilServiceLogin(installIdResponse, token);
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, BankServiceException, AuthorizationException {

        if (!jyskePersistentStorage.containsInstallId()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        Token token =
                jyskePersistentStorage
                        .getToken()
                        .orElseThrow(() -> SessionError.SESSION_EXPIRED.exception());

        NemIdLoginInstallIdEncryptionEntity installIdResponse =
                jyskePersistentStorage.getNemidLoginEntity();
        apiClient.nemIdInit(token);
        apiClient.nemIdLoginWithInstallId(installIdResponse, token);
        apiClient.sendTransportKey(token);
    }
}
