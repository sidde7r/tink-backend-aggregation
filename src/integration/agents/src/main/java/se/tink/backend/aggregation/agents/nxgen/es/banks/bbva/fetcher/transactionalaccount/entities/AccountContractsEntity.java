package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountContractsEntity {
    private ContractEntity contract;
    private SimpleAccountEntity account;

    public AccountContractsEntity(ContractEntity contract) {
        this.contract = contract;
    }

    public ContractEntity getContract() {
        return contract;
    }

    public void setContract(ContractEntity contract) {
        this.contract = contract;
    }

    public SimpleAccountEntity getAccount() {
        return account;
    }

    public void setAccount(SimpleAccountEntity account) {
        this.account = account;
    }
}
