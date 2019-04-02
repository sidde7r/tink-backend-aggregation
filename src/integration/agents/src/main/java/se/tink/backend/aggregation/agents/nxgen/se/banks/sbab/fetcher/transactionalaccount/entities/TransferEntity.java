package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransferEntity {
    @JsonProperty("counter_part_bank")
    private String counterPartBank;

    @JsonProperty("amount")
    private long amount;

    @JsonProperty("counter_part_account")
    private String counterPartAccount;

    @JsonProperty("statement")
    private String statement;

    @JsonProperty("recurring_transfer")
    private boolean recurringTransfer;

    @JsonProperty("transfer_id")
    private String transferId;

    @JsonProperty("type")
    private TransferTypeEntity type;

    @JsonProperty("running_balance")
    private String runningBalance;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("accounting_date")
    private Date accountingDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("value_date")
    private Date valueDate;

    @JsonProperty("status")
    private TransferStatusEntity status;

    @JsonProperty("counter_part_statement")
    private String counterPartStatement;

    public String getCounterPartBank() {
        return counterPartBank;
    }

    public long getAmount() {
        return amount;
    }

    public String getCounterPartAccount() {
        return counterPartAccount;
    }

    public String getStatement() {
        return statement;
    }

    public boolean isRecurringTransfer() {
        return recurringTransfer;
    }

    public String getTransferId() {
        return transferId;
    }

    public TransferTypeEntity getType() {
        return type;
    }

    public String getRunningBalance() {
        return runningBalance;
    }

    public Date getAccountingDate() {
        return accountingDate;
    }

    public Date getValueDate() {
        return valueDate;
    }

    public TransferStatusEntity getStatus() {
        return status;
    }

    public String getCounterPartStatement() {
        return counterPartStatement;
    }

    @JsonIgnore
    public Transaction toTinkTransaction() {
        final Amount tinkAmount;

        // TODO: Confirm INTEREST_RATE and OTHER
        switch (type) {
            case DEPOSIT:
            case INTEREST_RATE:
                tinkAmount = Amount.inSEK(amount);
                break;
            case WITHDRAWAL:
            case OTHER:
                tinkAmount = Amount.inSEK(amount).negate();
                break;
            default:
                tinkAmount = Amount.inSEK(amount).negate();
        }

        return Transaction.builder()
                .setAmount(tinkAmount)
                .setDescription(statement)
                .setDate(accountingDate)
                .build();
    }
}
