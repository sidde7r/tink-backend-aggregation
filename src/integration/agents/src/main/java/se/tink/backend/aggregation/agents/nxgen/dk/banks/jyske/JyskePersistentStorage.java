package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.KeycardChallengeEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdChallengeEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdLoginInstallIdEncryptionEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class JyskePersistentStorage {
    private final PersistentStorage persistentStorage;

    public JyskePersistentStorage(PersistentStorage storage) {
        this.persistentStorage = storage;
    }

    public Token generateToken() {
        Token token = Token.generate();
        persistentStorage.put(JyskeConstants.Storage.TOKEN, token);
        return token;
    }

    public Optional<Token> getToken() {
        return persistentStorage.get(JyskeConstants.Storage.TOKEN, Token.class);
    }

    public void invalidateToken() {
        persistentStorage.remove(JyskeConstants.Storage.TOKEN);
    }

    public void setUserId(String userId) {
        persistentStorage.put(JyskeConstants.Storage.USER_ID, userId);
    }

    public String getUserId() {
        return persistentStorage.get(JyskeConstants.Storage.USER_ID);
    }

    public void setPincode(String pincode) {
        persistentStorage.put(JyskeConstants.Storage.PIN_CODE, pincode);
    }

    public String getPincode() {
        return persistentStorage.get(JyskeConstants.Storage.PIN_CODE);
    }

    public void setKeycardChallengeEntity(KeycardChallengeEntity entity) {
        persistentStorage.put(JyskeConstants.Storage.KEYCARD_CHALLENGE_ENTITY, entity);
    }

    public KeycardChallengeEntity getKeycardChallengeEntity() {
        return persistentStorage
                .get(JyskeConstants.Storage.KEYCARD_CHALLENGE_ENTITY, KeycardChallengeEntity.class)
                .orElseThrow(
                        () -> new IllegalStateException("Can not find KeycardChallengeEntity"));
    }

    public void setChallengeEntity(NemIdChallengeEntity nemIdChallengeEntity) {
        persistentStorage.put(JyskeConstants.Storage.NEMID_CHALLENGE_ENTITY, nemIdChallengeEntity);
    }

    public NemIdChallengeEntity getChallengeEntity() {
        return persistentStorage
                .get(JyskeConstants.Storage.NEMID_CHALLENGE_ENTITY, NemIdChallengeEntity.class)
                .orElseThrow(() -> new IllegalStateException("Can not find challenge entity!"));
    }

    public void setNemidLoginEntity(NemIdLoginInstallIdEncryptionEntity response) {
        persistentStorage.put(JyskeConstants.Storage.NEMID_LOGIN_ENTITY, response);
    }

    public NemIdLoginInstallIdEncryptionEntity getNemidLoginEntity() {
        return persistentStorage
                .get(
                        JyskeConstants.Storage.NEMID_LOGIN_ENTITY,
                        NemIdLoginInstallIdEncryptionEntity.class)
                .orElseThrow(() -> new IllegalStateException("Can not find loginResponse!"));
    }

    public void setInstallId(String installId) {
        persistentStorage.put(JyskeConstants.Storage.INSTALL_ID, installId);
    }

    public String getInstallId() {
        return persistentStorage.get(JyskeConstants.Storage.INSTALL_ID);
    }

    public boolean containsInstallId() {
        return persistentStorage.containsKey(JyskeConstants.Storage.INSTALL_ID);
    }
}
