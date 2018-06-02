package se.tink.backend.aggregationcontroller.v1.rpc.entities;

import java.util.UUID;

public class TransferDestinationPattern {
    private UUID userId;
    private UUID accountId;
    private String type;
    private String pattern;
    private boolean matchesMultiple;
    private String name;
    private String bank;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public boolean isMatchesMultiple() {
        return matchesMultiple;
    }

    public void setMatchesMultiple(boolean matchesMultiple) {
        this.matchesMultiple = matchesMultiple;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }
}
