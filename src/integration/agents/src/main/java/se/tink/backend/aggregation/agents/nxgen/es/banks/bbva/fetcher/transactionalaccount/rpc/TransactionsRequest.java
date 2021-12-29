package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.UserEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.AccountContractsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.FilterEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsRequest {
    private UserEntity customer;
    private String searchText;
    private List<AccountContractsEntity> accountContracts;
    private String orderField;
    private String orderType;
    private FilterEntity filter;

    private TransactionsRequest(TransactionsRequestBuilder builder) {
        this.customer = builder.customer;
        this.searchText = builder.searchText;
        this.accountContracts = builder.accountContracts;
        this.orderField = builder.orderField;
        this.orderType = builder.orderType;
        this.filter = builder.filter;
    }

    public static TransactionsRequestBuilder builder() {
        return new TransactionsRequestBuilder();
    }

    public static class TransactionsRequestBuilder {
        private UserEntity customer;
        private String searchText;
        private List<AccountContractsEntity> accountContracts;
        private String orderField;
        private String orderType;
        private FilterEntity filter;

        public TransactionsRequestBuilder withCustomer(UserEntity customer) {
            this.customer = customer;
            return this;
        }

        public TransactionsRequestBuilder withSearchText(String searchText) {
            this.searchText = searchText;
            return this;
        }

        public TransactionsRequestBuilder withAccountContracts(
                List<AccountContractsEntity> accountContracts) {
            this.accountContracts = accountContracts;
            return this;
        }

        public TransactionsRequestBuilder withOrderField(String orderField) {
            this.orderField = orderField;
            return this;
        }

        public TransactionsRequestBuilder withOrderType(String orderType) {
            this.orderType = orderType;
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
