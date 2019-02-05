package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContractEntity {
    @JsonProperty("compteFacturation")
    private AccountEntity billingAccount;
    @JsonProperty("comptePrincipal")
    private AccountEntity mainAccount;
    @JsonProperty("comptes")
    private List<AccountEntity> accounts;

    public AccountEntity getBillingAccount() {
        return billingAccount;
    }

    public AccountEntity getMainAccount() {
        return mainAccount;
    }

    public List<AccountEntity> getAccounts() {
        return accounts;
    }
}
