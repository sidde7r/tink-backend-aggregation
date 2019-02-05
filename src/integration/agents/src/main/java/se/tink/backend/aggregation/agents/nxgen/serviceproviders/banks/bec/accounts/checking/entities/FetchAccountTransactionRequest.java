package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchAccountTransactionRequest {
    private String accountId;
    private String browseId;
    private Integer noOfRecords;
    private Integer searchFromAmount;
    private String searchFromDate;
    private String searchText;
    private Integer searchToAmount;
    private String searchToDate;
    private Boolean skipMatched;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getBrowseId() {
        return browseId;
    }

    public void setBrowseId(String browseId) {
        this.browseId = browseId;
    }

    public Integer getNoOfRecords() {
        return noOfRecords;
    }

    public void setNoOfRecords(Integer noOfRecords) {
        this.noOfRecords = noOfRecords;
    }

    public Integer getSearchFromAmount() {
        return searchFromAmount;
    }

    public void setSearchFromAmount(Integer searchFromAmount) {
        this.searchFromAmount = searchFromAmount;
    }

    public String getSearchFromDate() {
        return searchFromDate;
    }

    public void setSearchFromDate(String searchFromDate) {
        this.searchFromDate = searchFromDate;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public Integer getSearchToAmount() {
        return searchToAmount;
    }

    public void setSearchToAmount(Integer searchToAmount) {
        this.searchToAmount = searchToAmount;
    }

    public String getSearchToDate() {
        return searchToDate;
    }

    public void setSearchToDate(String searchToDate) {
        this.searchToDate = searchToDate;
    }

    public Boolean getSkipMatched() {
        return skipMatched;
    }

    public void setSkipMatched(Boolean skipMatched) {
        this.skipMatched = skipMatched;
    }
}
