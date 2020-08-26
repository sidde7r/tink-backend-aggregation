package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.entity.BusinessDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterPaymentRequest {
    private BusinessDataEntity businessData;
    private String language;
    private String signatureType;

    private RegisterPaymentRequest(
            BusinessDataEntity businessData, String language, String signatureType) {
        this.businessData = businessData;
        this.language = language;
        this.signatureType = signatureType;
    }

    public static RegisterPaymentRequest create(
            BusinessDataEntity businessData, String language, String signatureType) {
        return new RegisterPaymentRequest(businessData, language, signatureType);
    }
}
