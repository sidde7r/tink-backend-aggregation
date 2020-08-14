package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KeyCardPayloadScaEntity extends KeyCardPayload {
    private KeyCardChallengeEntity keycard;

    public KeyCardPayloadScaEntity(
            String userId,
            String pincode,
            String deviceId,
            KeyCardChallengeEntity keyCardChallengeEntity) {
        super(userId, pincode, deviceId);
        keycard = keyCardChallengeEntity;
    }

    public KeyCardChallengeEntity getKeycard() {
        return keycard;
    }

    public void setKeycard(KeyCardChallengeEntity keycard) {
        this.keycard = keycard;
    }
}
