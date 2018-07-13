package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.UserEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchTransactionsRequestEntity {
    private UserEntity customer;
    private String searchType;
    private List<AccountContractsEntity> accountContracts;

    public UserEntity getCustomer() {
        return customer;
    }

    public void setCustomer(UserEntity customer) {
        this.customer = customer;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public List<AccountContractsEntity> getAccountContracts() {
        return accountContracts;
    }

    public void setAccountContracts(List<AccountContractsEntity> accountContracts) {
        this.accountContracts = accountContracts;
    }
}
