package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.ScaOptions;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KeyCardPayloadScaEntity extends GeneralPayload {
    private KeyCardChallengeEntity keycard;

    public KeyCardPayloadScaEntity(
            String userId,
            String pincode,
            String deviceId,
            KeyCardChallengeEntity keyCardChallengeEntity,
            String userLocale) {
        super(userId, pincode, deviceId, ScaOptions.KEYCARD_OPTION, userLocale);
        keycard = keyCardChallengeEntity;
    }

    public KeyCardChallengeEntity getKeycard() {
        return keycard;
    }

    public void setKeycard(KeyCardChallengeEntity keycard) {
        this.keycard = keycard;
    }
}
