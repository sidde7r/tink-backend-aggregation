package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.ScaOptions;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CodeAppPayloadScaEntity extends GeneralPayload {
    private CodeAppScaEntity codeapp;

    public CodeAppPayloadScaEntity(
            String userId,
            String pincode,
            String deviceId,
            CodeAppScaEntity codeapp,
            String userLocale) {
        super(userId, pincode, deviceId, ScaOptions.CODEAPP_OPTION, userLocale);
        this.codeapp = codeapp;
    }

    public CodeAppScaEntity getCodeapp() {
        return codeapp;
    }

    public void setCodeapp(CodeAppScaEntity codeapp) {
        this.codeapp = codeapp;
    }
}
