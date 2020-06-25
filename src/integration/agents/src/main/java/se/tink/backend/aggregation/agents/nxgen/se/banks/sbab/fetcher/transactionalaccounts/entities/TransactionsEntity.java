package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities;

import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionsEntity {
    private String accountNumberFrom;
    private String accountNumberTo;
    private BigDecimal amount;
    private String bankNameFrom;
    private String bankNameTo;
    private String narrativeFrom;
    private String narrativeTo;
    private BigDecimal runningBalance;
    private String transactionIdentifier;
    private String transferType;
    private Date valueDate;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(getAmount())
                .setType(getTransferType())
                .setRawDetails(getRawDetails())
                .setDate(getValueDate())
                .setDescription(getDetails())
                .build();
    }

    public String getDetails() {
        return getNarrativeFrom() + getAccountNumberTo();
    }

    public String getRawDetails() {
        return getAccountNumberFrom()
                + getAccountNumberTo()
                + getBankNameFrom()
                + getBankNameTo()
                + getTransactionIdentifier();
    }

    public String getAccountNumberFrom() {
        return accountNumberFrom;
    }

    public String getAccountNumberTo() {
        return accountNumberTo;
    }

    public ExactCurrencyAmount getAmount() {
        return ExactCurrencyAmount.of(amount, SBABConstants.CURRENCY);
    }

    public String getBankNameFrom() {
        return bankNameFrom;
    }

    public String getBankNameTo() {
        return bankNameTo;
    }

    public String getNarrativeFrom() {
        return narrativeFrom;
    }

    public String getNarrativeTo() {
        return narrativeTo;
    }

    public BigDecimal getRunningBalance() {
        return runningBalance;
    }

    public String getTransactionIdentifier() {
        return transactionIdentifier;
    }

    public TransactionTypes getTransferType() {
        return SBABConstants.TRANSACTION_TYPES.get(transferType);
    }

    public Date getValueDate() {
        return valueDate;
    }
}
