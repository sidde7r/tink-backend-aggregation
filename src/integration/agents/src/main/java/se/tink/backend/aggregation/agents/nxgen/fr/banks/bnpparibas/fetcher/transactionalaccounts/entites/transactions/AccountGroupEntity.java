package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountGroupEntity {
    @JsonProperty("libelleFamilleCompte")
    private String accountGroupLabel;
    @JsonProperty("idFamilleCompte")
    private int accountGroupId;

    public String getAccountGroupLabel() {
        return accountGroupLabel;
    }

    public int getAccountGroupId() {
        return accountGroupId;
    }
}
