package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

public interface AssertionData {

    @JsonObject
    @AllArgsConstructor
    class OtpEntity implements AssertionData {
        private String otp;
    }

    @JsonObject
    @AllArgsConstructor
    class PublicKeyEntity implements AssertionData {
        private String key;
        private String type;
    }
}
