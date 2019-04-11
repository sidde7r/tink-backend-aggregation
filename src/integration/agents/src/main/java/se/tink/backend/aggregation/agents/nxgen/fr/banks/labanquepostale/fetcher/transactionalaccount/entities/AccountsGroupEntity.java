package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsGroupEntity {

    @JsonProperty("totalSolde")
    private double totalBalance;

    @JsonProperty("comptes")
    private List<AccountEntity> accounts;

    public List<AccountEntity> getAccounts() {
        return accounts != null ? accounts : Collections.emptyList();
    }
}
