package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.entity.KeyEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EnrollmentRequest {
    private String nickname;
    private KeyEntity key;
    private String type;

    @JsonProperty("signing_type")
    private String signingType;

    public static EnrollmentRequest create(String deviceName, String type, KeyEntity publicKey) {
        EnrollmentRequest enrollmentRequest = new EnrollmentRequest();
        enrollmentRequest.nickname = deviceName;
        enrollmentRequest.signingType = "ndf";
        enrollmentRequest.type = type;
        enrollmentRequest.key = publicKey;
        return enrollmentRequest;
    }
}
