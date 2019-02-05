package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankdataTransactionEntity {
    private BankdataAccountIdEntity accountId;
    private List<Map<String, Object>> splits;
    private String identifier;
    private String currency;
    private double balance;
    private boolean unCleared;
    private boolean flagged;
    private boolean balanced;
    private boolean message;
    private boolean attachment;
    private List<Map<String, Object>> tags;
    private String text;
    private double mainAmount;
    private String valeurDate;
    private String transactionDate;
    private boolean stoppable;
    private double newMaxSplitAmount;
    private int parentCategoryId;
    private boolean split;
    private boolean partial;
    private boolean outOfSyncTransaction;
    private int status;
    private boolean canVerify;
    private boolean reservation;

    public BankdataAccountIdEntity getAccountId() {
        return accountId;
    }

    public List<Map<String, Object>> getSplits() {
        return splits;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getCurrency() {
        return currency;
    }

    public double getBalance() {
        return balance;
    }

    public boolean isUnCleared() {
        return unCleared;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public boolean isBalanced() {
        return balanced;
    }

    public boolean isMessage() {
        return message;
    }

    public boolean isAttachment() {
        return attachment;
    }

    public List<Map<String, Object>> getTags() {
        return tags;
    }

    public String getText() {
        return text;
    }

    public double getMainAmount() {
        return mainAmount;
    }

    public String getValeurDate() {
        return valeurDate;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public boolean isStoppable() {
        return stoppable;
    }

    public double getNewMaxSplitAmount() {
        return newMaxSplitAmount;
    }

    public int getParentCategoryId() {
        return parentCategoryId;
    }

    public boolean isSplit() {
        return split;
    }

    public boolean isPartial() {
        return partial;
    }

    public boolean isOutOfSyncTransaction() {
        return outOfSyncTransaction;
    }

    public int getStatus() {
        return status;
    }

    public boolean isCanVerify() {
        return canVerify;
    }

    public boolean isReservation() {
        return reservation;
    }
}
