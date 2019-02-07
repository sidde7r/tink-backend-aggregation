package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class OpBankCreditCardTransaction {
    public String transactionDate;
    public String explanation;
    public String amount;
    private int status;
    private String notEuroAmount;
    private String timestamp;
    private String cardNumber;
    private String lineReference;

    @JsonIgnore
    public CreditCardTransaction toTinkCreditCardTransaction(CreditCardAccount account) {
        return CreditCardTransaction.builder()
                .setDate(getTransactionDateParsed())
                .setAmount(amountToAmount())
                .setDescription(getDescription())
                .setCreditAccount(account)
                .build();
    }

    @JsonIgnore
    private String getDescription() {
        if (explanation != null) {
            return explanation.trim();
        }

        return null;
    }

    @JsonIgnore
    public Amount amountToAmount() {
        return Amount.inEUR(AgentParsingUtils.parseAmount(amount));
    }

    @JsonIgnore
    public boolean isValidTransaction() {
        return transactionDate != null && amount != null && explanation != null;
    }

    @JsonIgnore
    public boolean isDuplicateTransaction(List<OpBankCreditCardTransaction> processedTransactions) {
        return processedTransactions.stream().anyMatch(transaction -> this.equals(transaction));
    }

    @JsonIgnore
    public Date getTransactionDateParsed() {
        try {
            return ThreadSafeDateFormat.FORMATTER_DOTTED_DAILY.parse(transactionDate);
        } catch (ParseException e) {
            throw new IllegalStateException("Unable to parse transaction date: " + transactionDate, e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OpBankCreditCardTransaction)) {
            return false;
        }

        OpBankCreditCardTransaction other = (OpBankCreditCardTransaction) obj;

        if (amount == null && other.amount != null) {
            return false;
        }

        if (!amount.equals(other.amount)) {
            return false;
        }

        if (transactionDate == null && other.transactionDate != null) {
            return false;
        }

        if (!transactionDate.equals(other.transactionDate)) {
            return false;
        }

        if (explanation == null && other.explanation != null) {
            return false;
        }

        if (!explanation.equals(other.explanation)) {
            return false;
        }

        if (status != other.status) {
            return false;
        }

        return true;
    }
}
