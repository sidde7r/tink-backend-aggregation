package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountListResponse {
    private List<AccountEntity> accounts;
    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;
    @JsonProperty("_savingsAccountaggregatedAmountFraction")
    private String savingsAccountaggregatedAmountFraction;
    @JsonProperty("_savingsAccountaggregatedAmountInteger")
    private String savingsAccountaggregatedAmountInteger;
    @JsonProperty("_currentAccountaggregatedAmountFraction")
    private String currentAccountaggregatedAmountFraction;
    @JsonProperty("_currentAccountaggregatedAmountInteger")
    private String currentAccountaggregatedAmountInteger;
    @JsonProperty("_currentAndSavingAccountaggregatedAmountFraction")
    private String currentAndSavingAccountaggregatedAmountFraction;
    @JsonProperty("_currentAndSavingAccountaggregatedAmountInteger")
    private String currentAndSavingAccountaggregatedAmountInteger;
    @JsonProperty("_disposableAccountaggregatedAmountFraction")
    private String disposableAccountaggregatedAmountFraction;
    @JsonProperty("_disposableAccountaggregatedAmountInteger")
    private String disposableAccountaggregatedAmountInteger;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    public void setAccounts(
            List<AccountEntity> accounts) {
        this.accounts = accounts;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }

    public void setLinks(
            HashMap<String, LinkEntity> links) {
        this.links = links;
    }

    public String getSavingsAccountaggregatedAmountFraction() {
        return savingsAccountaggregatedAmountFraction;
    }

    public void setSavingsAccountaggregatedAmountFraction(String savingsAccountaggregatedAmountFraction) {
        this.savingsAccountaggregatedAmountFraction = savingsAccountaggregatedAmountFraction;
    }

    public String getSavingsAccountaggregatedAmountInteger() {
        return savingsAccountaggregatedAmountInteger;
    }

    public void setSavingsAccountaggregatedAmountInteger(String savingsAccountaggregatedAmountInteger) {
        this.savingsAccountaggregatedAmountInteger = savingsAccountaggregatedAmountInteger;
    }

    public String getCurrentAccountaggregatedAmountFraction() {
        return currentAccountaggregatedAmountFraction;
    }

    public void setCurrentAccountaggregatedAmountFraction(String currentAccountaggregatedAmountFraction) {
        this.currentAccountaggregatedAmountFraction = currentAccountaggregatedAmountFraction;
    }

    public String getCurrentAccountaggregatedAmountInteger() {
        return currentAccountaggregatedAmountInteger;
    }

    public void setCurrentAccountaggregatedAmountInteger(String currentAccountaggregatedAmountInteger) {
        this.currentAccountaggregatedAmountInteger = currentAccountaggregatedAmountInteger;
    }

    public String getCurrentAndSavingAccountaggregatedAmountFraction() {
        return currentAndSavingAccountaggregatedAmountFraction;
    }

    public void setCurrentAndSavingAccountaggregatedAmountFraction(
            String currentAndSavingAccountaggregatedAmountFraction) {
        this.currentAndSavingAccountaggregatedAmountFraction = currentAndSavingAccountaggregatedAmountFraction;
    }

    public String getCurrentAndSavingAccountaggregatedAmountInteger() {
        return currentAndSavingAccountaggregatedAmountInteger;
    }

    public void setCurrentAndSavingAccountaggregatedAmountInteger(
            String currentAndSavingAccountaggregatedAmountInteger) {
        this.currentAndSavingAccountaggregatedAmountInteger = currentAndSavingAccountaggregatedAmountInteger;
    }

    public String getDisposableAccountaggregatedAmountFraction() {
        return disposableAccountaggregatedAmountFraction;
    }

    public void setDisposableAccountaggregatedAmountFraction(String disposableAccountaggregatedAmountFraction) {
        this.disposableAccountaggregatedAmountFraction = disposableAccountaggregatedAmountFraction;
    }

    public String getDisposableAccountaggregatedAmountInteger() {
        return disposableAccountaggregatedAmountInteger;
    }

    public void setDisposableAccountaggregatedAmountInteger(String disposableAccountaggregatedAmountInteger) {
        this.disposableAccountaggregatedAmountInteger = disposableAccountaggregatedAmountInteger;
    }
}
