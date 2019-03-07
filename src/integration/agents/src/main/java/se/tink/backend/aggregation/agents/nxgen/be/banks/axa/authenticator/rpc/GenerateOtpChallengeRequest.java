package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.entities.InputEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class GenerateOtpChallengeRequest {
    private InputEntity input;

    private GenerateOtpChallengeRequest() {}

    private GenerateOtpChallengeRequest(InputEntity entity) {
        input = entity;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String serialNo;
        private String applcd;
        private String language;

        private Builder() {}

        public Builder setSerialNo(String serialNo) {
            this.serialNo = serialNo;
            return this;
        }

        public Builder setApplcd(String applcd) {
            this.applcd = applcd;
            return this;
        }

        public Builder setLanguage(String language) {
            this.language = language;
            return this;
        }

        public GenerateOtpChallengeRequest build() {
            InputEntity entity = new InputEntity();
            entity.setSerialNo(serialNo);
            entity.setApplCd(applcd);
            entity.setLanguage(language);
            return new GenerateOtpChallengeRequest(entity);
        }
    }
}
