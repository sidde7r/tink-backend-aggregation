package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskePersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdChallengeEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdEnrollEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdInstallIdEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdLoginEncryptionEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.rpc.NemIdResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Decryptor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Token;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardInitValues;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class JyskeKeyCardAuthenticator implements KeyCardAuthenticator {

    private final JyskeApiClient apiClient;
    private final JyskePersistentStorage persistentStorage;
    private final JyskeServiceAuthenticator serviceAuthenticator;

    private NemIdChallengeEntity challengeEntity;
    private String username;
    private String password;
    private Token token;

    public JyskeKeyCardAuthenticator(
            JyskeApiClient client, JyskePersistentStorage persistentStorage) {
        this.apiClient = client;
        this.persistentStorage = persistentStorage;
        serviceAuthenticator = new JyskeServiceAuthenticator(apiClient);
    }

    @Override
    public KeyCardInitValues init(String username, String password) throws AuthenticationException {

        this.username = username;
        this.password = password;
        this.token = Token.generate();

        apiClient.nemIdInit(this.token);

        try {
            NemIdLoginEncryptionEntity encryptionEntity = new NemIdLoginEncryptionEntity();
            encryptionEntity.setUserId(username);
            encryptionEntity.setPinCode(password);
            NemIdResponse challenge = apiClient.nemIdGetChallenge(encryptionEntity, token);
            this.challengeEntity = new Decryptor(token).read(challenge, NemIdChallengeEntity.class);
            return new KeyCardInitValues(
                    this.challengeEntity.getKeycardNo(), this.challengeEntity.getKey());
        } catch (HttpResponseException e) {
            NemIdErrorEntity.throwError(e);
            throw e; // will never get here because exception already thrown.
        }
    }

    @Override
    public void authenticate(String code) throws AuthenticationException, AuthorizationException {

        NemIdEnrollEntity enrollEntity = new NemIdEnrollEntity();
        enrollEntity.setKey(code);
        enrollEntity.setSecurityDevice(this.challengeEntity.getSecurityDevice());
        enrollEntity.setKeyNo(this.challengeEntity.getKey());
        enrollEntity.setKeycardNo(this.challengeEntity.getKeycardNo());
        enrollEntity.setMobileCode(this.password);

        NemIdResponse enrollment = apiClient.nemIdEnroll(enrollEntity, this.token);

        NemIdInstallIdEntity installIdEntity =
                new Decryptor(token).read(enrollment, NemIdInstallIdEntity.class);

        NemIdLoginEncryptionEntity encryptionEntity = new NemIdLoginEncryptionEntity();

        String installId = installIdEntity.getInstallId();
        encryptionEntity.setInstallId(installId);
        encryptionEntity.setUserId(username);
        encryptionEntity.setPinCode(password);

        try {
            NemIdResponse encryption = apiClient.nemIdLoginWithInstallId(encryptionEntity, token);
            serviceAuthenticator.authenticate(encryption, token);
        } catch (HttpResponseException e) {
            NemIdErrorEntity.throwError(e);
        }

        persistentStorage.persist(installId);
    }
}
