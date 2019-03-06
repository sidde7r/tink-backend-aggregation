package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.entities.InputEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class GetTransactionsRequest {
    private InputEntity input;

    private GetTransactionsRequest(final InputEntity entity) {
        input = entity;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String applCd;
        private int customerId;
        private String language;
        private String directionFlag;
        private String accountNumber;
        private String referenceNumber;
        private String transactionCode;
        private String updateTimestamp;

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

        public Builder setDirectionFlag(String directionFlag) {
            this.directionFlag = directionFlag;
            return this;
        }

        public Builder setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public Builder setReferenceNumber(String referenceNumber) {
            this.referenceNumber = referenceNumber;
            return this;
        }

        public Builder setTransactionCode(String transactionCode) {
            this.transactionCode = transactionCode;
            return this;
        }

        public Builder setUpdateTimestamp(String updateTimestamp) {
            this.updateTimestamp = updateTimestamp;
            return this;
        }

        public GetTransactionsRequest build() {
            final InputEntity entity = new InputEntity();
            entity.setApplCd(applCd);
            entity.setCustomerId(customerId);
            entity.setLanguage(language);
            entity.setDirectionFlag(directionFlag);
            entity.setAccountNumber(accountNumber);
            entity.setReferenceNumber(referenceNumber);
            entity.setTransactionCode(transactionCode);
            entity.setUpdateTimestamp(updateTimestamp);
            return new GetTransactionsRequest(entity);
        }
    }
}
