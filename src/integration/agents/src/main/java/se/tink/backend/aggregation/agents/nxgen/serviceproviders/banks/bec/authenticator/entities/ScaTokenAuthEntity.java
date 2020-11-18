package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.ScaOptions.SCATOKEN_OPTION;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ScaTokenAuthEntity extends GeneralPayload {
    private ScaTokenEntity scatoken;

    public ScaTokenAuthEntity(
            String userId,
            String pincode,
            String deviceId,
            ScaTokenEntity scaTokenEntity,
            String userLocale) {
        super(userId, pincode, deviceId, SCATOKEN_OPTION, userLocale);
        this.scatoken = scaTokenEntity;
    }
}
