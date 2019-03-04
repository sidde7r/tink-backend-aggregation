package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.entities.InputEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class GetAccountsRequest {
    private InputEntity input;

    private GetAccountsRequest() {}

    private GetAccountsRequest(final InputEntity entity) {
        input = entity;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
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

        public GetAccountsRequest build() {
            final InputEntity entity = new InputEntity();
            entity.setApplCd(applCd);
            entity.setCustomerId(customerId);
            entity.setLanguage(language);
            return new GetAccountsRequest(entity);
        }
    }
}
