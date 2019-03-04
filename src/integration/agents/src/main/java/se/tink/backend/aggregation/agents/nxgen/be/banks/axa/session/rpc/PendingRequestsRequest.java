package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.session.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.InputEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class PendingRequestsRequest {
    private InputEntity input;

    private PendingRequestsRequest(final InputEntity entity) {
        input = entity;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String applCd;
        private int customerId;
        private String language;

        public Builder setApplCd(String applCd) {
            this.applCd = applCd;
            return this;
        }

        public Builder setCustomerId(int customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder setLanguage(String language) {
            this.language = language;
            return this;
        }

        public PendingRequestsRequest build() {
            final InputEntity entity = new InputEntity();
            entity.setApplCd(applCd);
            entity.setCustomerId(customerId);
            entity.setLanguage(language);
            return new PendingRequestsRequest(entity);
        }
    }
}
