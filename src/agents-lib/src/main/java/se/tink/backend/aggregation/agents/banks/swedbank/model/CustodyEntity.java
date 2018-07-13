package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustodyEntity {
    private String id;
    private Boolean blocked;
    private Boolean disposed;
    private String clearingNumber;
    private Boolean defaultTradingAccount;
    private String custodyNumber;
    private String custodyName;
    private String holdingAccountId;
    private LinksEntity links;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getBlocked() {
        return blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

    public Boolean getDisposed() {
        return disposed;
    }

    public void setDisposed(Boolean disposed) {
        this.disposed = disposed;
    }

    public String getClearingNumber() {
        return clearingNumber;
    }

    public void setClearingNumber(String clearingNumber) {
        this.clearingNumber = clearingNumber;
    }

    public Boolean getDefaultTradingAccount() {
        return defaultTradingAccount;
    }

    public void setDefaultTradingAccount(Boolean defaultTradingAccount) {
        this.defaultTradingAccount = defaultTradingAccount;
    }

    public String getCustodyNumber() {
        return custodyNumber;
    }

    public void setCustodyNumber(String custodyNumber) {
        this.custodyNumber = custodyNumber;
    }

    public String getCustodyName() {
        return custodyName;
    }

    public void setCustodyName(String custodyName) {
        this.custodyName = custodyName;
    }

    public String getHoldingAccountId() {
        return holdingAccountId;
    }

    public void setHoldingAccountId(String holdingAccountId) {
        this.holdingAccountId = holdingAccountId;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public void setLinks(LinksEntity links) {
        this.links = links;
    }
}
