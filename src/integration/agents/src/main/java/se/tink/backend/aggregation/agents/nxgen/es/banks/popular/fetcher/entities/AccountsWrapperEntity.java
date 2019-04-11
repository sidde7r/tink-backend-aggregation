package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsWrapperEntity {
    @JsonProperty("masOperaciones")
    private int moreOperations;

    private int noccursdatostab;

    @JsonProperty("customRr001014")
    private List<AccountEntity> accountList;

    public int getMoreOperations() {
        return moreOperations;
    }

    public int getNoccursdatostab() {
        return noccursdatostab;
    }

    public List<AccountEntity> getAccountList() {
        return accountList;
    }
}
