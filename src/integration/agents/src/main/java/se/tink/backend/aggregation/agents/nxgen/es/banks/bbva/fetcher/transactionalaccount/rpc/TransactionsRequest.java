package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.UserEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.AccountContractsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.FilterEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsRequest {
    private UserEntity customer;
    private String searchType;
    private List<AccountContractsEntity> accountContracts;
    private Boolean error;
    private FilterEntity filter;

    private TransactionsRequest(TransactionsRequestBuilder builder) {
        this.customer = builder.customer;
        this.searchType = builder.searchType;
        this.accountContracts = builder.accountContracts;
        this.error = builder.error;
        this.filter = builder.filter;
    }

    public static TransactionsRequestBuilder builder() {
        return new TransactionsRequestBuilder();
    }

    public static class TransactionsRequestBuilder {
        private UserEntity customer;
        private String searchType;
        private List<AccountContractsEntity> accountContracts;
        private Boolean error;
        private FilterEntity filter;

        public TransactionsRequestBuilder withCustomer(UserEntity customer) {
            this.customer = customer;
            return this;
        }

        public TransactionsRequestBuilder withSearchType(String searchType) {
            this.searchType = searchType;
            return this;
        }

        public TransactionsRequestBuilder withAccountContracts(
                List<AccountContractsEntity> accountContracts) {
            this.accountContracts = accountContracts;
            return this;
        }

        public TransactionsRequestBuilder withError(Boolean error) {
            this.error = error;
            return this;
        }

        public TransactionsRequestBuilder withFilter(FilterEntity filter) {
            this.filter = filter;
            return this;
        }

        public TransactionsRequest build() {
            return new TransactionsRequest(this);
        }
    }
}
