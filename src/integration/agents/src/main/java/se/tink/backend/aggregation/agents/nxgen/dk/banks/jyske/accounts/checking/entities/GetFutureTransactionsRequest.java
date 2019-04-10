package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetFutureTransactionsRequest {
    private List<AccountBriefEntity> accounts;
    private Integer page;

    public List<AccountBriefEntity> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountBriefEntity> accounts) {
        this.accounts = accounts;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }
}
