package se.tink.analytics.jobs.loandata;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

public class LoanData implements Serializable {

    private static final long serialVersionUID = -1028184213887072561L;

    private UUID accountId;
    private UUID id;
    private UUID userId;
    private Integer numMonthsBound;
    private String name;
    private String type;
    private Double interest;
    private Double balance;
    private Boolean resolved;
    private String providerName;

    public static Map<String, String> getColumnMap() {
        Map<String, String> map = Maps.newHashMap();
        map.put("accountId", "accountid");
        map.put("userId", "userid");
        map.put("numMonthsBound", "nummonthsbound");
        map.put("providerName", "providername");

        return map;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Integer getNumMonthsBound() {
        return numMonthsBound;
    }

    public void setNumMonthsBound(Integer numMonthsBound) {
        this.numMonthsBound = numMonthsBound;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getInterest() {
        return interest;
    }

    public void setInterest(Double interest) {
        this.interest = interest;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Boolean getResolved() {
        return resolved;
    }

    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
