package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.entities.InputEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class GenerateChallengeRequest {
    private InputEntity input;

    private GenerateChallengeRequest() {}

    private GenerateChallengeRequest(InputEntity entity) {
        input = entity;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String ucrid;
        private String applcd;
        private String language;

        private Builder() {}

        public Builder setUcrid(String ucrid) {
            this.ucrid = ucrid;
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

        public GenerateChallengeRequest build() {
            InputEntity entity = new InputEntity();
            entity.setuCRid(ucrid);
            entity.setApplCd(applcd);
            entity.setLanguage(language);
            return new GenerateChallengeRequest(entity);
        }
    }
}
