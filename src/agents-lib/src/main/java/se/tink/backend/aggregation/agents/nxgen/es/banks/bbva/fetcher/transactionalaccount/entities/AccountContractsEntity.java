package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountContractsEntity {

    private ContractEntity contract;
    private SimpleAccountEntity account;

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

    @JsonIgnore
    public boolean isContractId(String id) {
        return Objects.nonNull(contract) && id.equalsIgnoreCase(contract.getId());
    }

    @JsonIgnore
    public Amount getAvailableBalanceAsTinkAmount() {
        return account.getCurrentBalance()
                .getAvailableBalance()
                .getTinkAmount();
    }
}
