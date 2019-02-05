package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionTypeEntity {
    @JsonProperty("codeFamille")
    private int groupCode;
    @JsonProperty("libelle")
    private String label;

    public int getGroupCode() {
        return groupCode;
    }

    public String getLabel() {
        return label;
    }
}
