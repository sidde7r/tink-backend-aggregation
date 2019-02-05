package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetTransactionsRequest {
    private List<AccountBriefEntity> accounts;
    private Integer page;
    private Boolean showPlanning;

    public List<AccountBriefEntity> getAccounts() {
        return accounts;
    }

    public void setAccounts(
            List<AccountBriefEntity> accounts) {
        this.accounts = accounts;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Boolean getShowPlanning() {
        return showPlanning;
    }

    public void setShowPlanning(Boolean showPlanning) {
        this.showPlanning = showPlanning;
    }
}
