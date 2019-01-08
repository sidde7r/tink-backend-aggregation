package se.tink.backend.aggregation.agents.brokers.lysa.model;

import java.util.Date;
import java.util.Map;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;

@JsonObject
public class AccountEntity {
    private String accountId;
    private double cash;
    private double risk;
    private double worth;
    private String name;
    private Date created;
    private String owner;
    private Map<String,Double> target;
    private Map<String,PositionEntity> positions;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public double getCash() {
        return cash;
    }

    public void setCash(double cash) {
        this.cash = cash;
    }

    public double getRisk() {
        return risk;
    }

    public void setRisk(double risk) {
        this.risk = risk;
    }

    public double getWorth() {
        return worth;
    }

    public void setWorth(double worth) {
        this.worth = worth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Map<String, Double> getTarget() {
        return target;
    }

    public void setTarget(Map<String, Double> target) {
        this.target = target;
    }

    public Map<String, PositionEntity> getPositions() {
        return positions;
    }

    public void setPositions(Map<String, PositionEntity> positions) {
        this.positions = positions;
    }

    public Account toAccount() {
        Account a = new Account();

        a.setBalance(worth+cash);
        a.setName(name);
        a.setType(AccountTypes.INVESTMENT);
        a.setBankId(accountId);

        return a;
    }
}
