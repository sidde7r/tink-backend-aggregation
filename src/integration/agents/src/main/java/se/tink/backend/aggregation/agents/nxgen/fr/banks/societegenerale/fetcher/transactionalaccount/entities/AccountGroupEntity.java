package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountGroupEntity {

    @JsonProperty("codeGroupe")
    private String groupCode;
    @JsonProperty("prestations")
    private List<AccountEntity> benefits;

    public List<AccountEntity> getBenefits() {
        return benefits;
    }

}
