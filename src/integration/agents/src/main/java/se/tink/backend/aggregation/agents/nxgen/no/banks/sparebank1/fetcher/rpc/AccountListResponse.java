package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
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
        return accounts == null ? Collections.emptyList() : accounts;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }

    public String getSavingsAccountaggregatedAmountFraction() {
        return savingsAccountaggregatedAmountFraction;
    }

    public String getSavingsAccountaggregatedAmountInteger() {
        return savingsAccountaggregatedAmountInteger;
    }

    public String getCurrentAccountaggregatedAmountFraction() {
        return currentAccountaggregatedAmountFraction;
    }

    public String getCurrentAccountaggregatedAmountInteger() {
        return currentAccountaggregatedAmountInteger;
    }

    public String getCurrentAndSavingAccountaggregatedAmountFraction() {
        return currentAndSavingAccountaggregatedAmountFraction;
    }

    public String getCurrentAndSavingAccountaggregatedAmountInteger() {
        return currentAndSavingAccountaggregatedAmountInteger;
    }

    public String getDisposableAccountaggregatedAmountFraction() {
        return disposableAccountaggregatedAmountFraction;
    }

    public String getDisposableAccountaggregatedAmountInteger() {
        return disposableAccountaggregatedAmountInteger;
    }
}
