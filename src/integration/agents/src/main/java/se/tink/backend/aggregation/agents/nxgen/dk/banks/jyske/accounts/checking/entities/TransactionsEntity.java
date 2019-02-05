package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.entities;

import java.text.ParseException;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class TransactionsEntity {
    private AccountEntity account;
    private String identifier;
    private String currency;
    private Double balance;
    private Boolean unCleared;
    private Boolean flagged;
    private Boolean balanced;
    private Boolean message;
    private Boolean attachment;
    private String text;
    private Double mainAmount;
    private String valeurDate;
    private String transactionDate;
    private Boolean stoppable;
    private Double newMaxSplitAmount;
    private Integer parentCategoryId;
    private Boolean split;
    private Boolean partial;
    private Boolean outOfSyncTransaction;
    private Integer status;
    private Boolean canVerify;
    private Boolean reservation;

    public AccountEntity getAccount() {
        return account;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getCurrency() {
        return currency;
    }

    public Double getBalance() {
        return balance;
    }

    public Boolean getUnCleared() {
        return unCleared;
    }

    public Boolean getFlagged() {
        return flagged;
    }

    public Boolean getBalanced() {
        return balanced;
    }

    public Boolean getMessage() {
        return message;
    }

    public Boolean getAttachment() {
        return attachment;
    }

    public String getText() {
        return text;
    }

    public Double getMainAmount() {
        return mainAmount;
    }

    public String getValeurDate() {
        return valeurDate;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public Boolean getStoppable() {
        return stoppable;
    }

    public Double getNewMaxSplitAmount() {
        return newMaxSplitAmount;
    }

    public Integer getParentCategoryId() {
        return parentCategoryId;
    }

    public Boolean getSplit() {
        return split;
    }

    public Boolean getPartial() {
        return partial;
    }

    public Boolean getOutOfSyncTransaction() {
        return outOfSyncTransaction;
    }

    public Integer getStatus() {
        return status;
    }

    public Boolean getCanVerify() {
        return canVerify;
    }

    public Boolean getReservation() {
        return reservation;
    }

    public Transaction toTinkTransaction() {
        try {
            return Transaction.builder()
                    .setDescription(getText())
                    .setDate(ThreadSafeDateFormat.FORMATTER_DOTTED_DAILY.parse(transactionDate))
                    .setAmount(Amount.inDKK(getMainAmount()))
                    .build();
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public UpcomingTransaction toTinkUpcomingTransaction() {
        try {
            return UpcomingTransaction.builder()
                    .setDescription(getText())
                    .setDate(ThreadSafeDateFormat.FORMATTER_DOTTED_DAILY.parse(transactionDate))
                    .setAmount(Amount.inDKK(getMainAmount()))
                    .build();
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
