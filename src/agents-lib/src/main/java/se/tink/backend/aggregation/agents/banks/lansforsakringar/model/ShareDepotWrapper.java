package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShareDepotWrapper {
    private ShareDepotEntity depot;
    private DepotAccountEntity account;

    public ShareDepotEntity getDepot() {
        return depot;
    }

    public void setDepot(ShareDepotEntity depot) {
        this.depot = depot;
    }

    public DepotAccountEntity getAccount() {
        return account;
    }

    public void setAccount(DepotAccountEntity account) {
        this.account = account;
    }
}
