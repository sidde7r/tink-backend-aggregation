package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.entities.AccountHistoryParameters;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.entities.InputEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.entities.PagingCriteria;
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
        private String accountReferenceNumber;

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

        public Builder setAccountReferenceNumber(String accountReferenceNumber) {
            this.accountReferenceNumber = accountReferenceNumber;
            return this;
        }

        public GetTransactionsRequest build() {
            final InputEntity entity = new InputEntity();
            entity.setApplCd(applCd);
            entity.setCustomerId(customerId);
            entity.setLanguage(language);
            entity.setAccountReferenceNumber(accountReferenceNumber);
            entity.setIncludePendingOrders(true);
            entity.setIncludeRefusedTransfers(true);
            entity.setIncludeSavingOrders(true);
            entity.setIncludeStandingOrders(true);
            entity.setIncludeTransactionsInExecution(true);
            entity.setAccountHistoryParameters(new AccountHistoryParameters(new PagingCriteria()));
            return new GetTransactionsRequest(entity);
        }
    }
}
