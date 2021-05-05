package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class OpBankCreditCardTransaction {
    private String transactionDate;
    private String explanation;
    private String amount;
    private int status;
    private String notEuroAmount;
    private String timestamp;
    private String cardNumber;
    private String lineReference;

    @JsonIgnore
    public CreditCardTransaction toTinkCreditCardTransaction(CreditCardAccount account) {
        return CreditCardTransaction.builder()
                .setDate(getTransactionDateParsed())
                .setAmount(ExactCurrencyAmount.inEUR(AgentParsingUtils.parseAmount(amount)))
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
    public boolean isValidTransaction() {
        return transactionDate != null && amount != null && explanation != null;
    }

    @JsonIgnore
    public boolean isDuplicateTransaction(List<OpBankCreditCardTransaction> processedTransactions) {
        return processedTransactions.stream().anyMatch(this::equals);
    }

    @JsonIgnore
    private Date getTransactionDateParsed() {
        try {
            return ThreadSafeDateFormat.FORMATTER_DOTTED_DAILY.parse(transactionDate);
        } catch (ParseException e) {
            throw new IllegalStateException(
                    "Unable to parse transaction date: " + transactionDate, e);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, transactionDate, explanation, status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OpBankCreditCardTransaction)) {
            return false;
        }
        OpBankCreditCardTransaction that = (OpBankCreditCardTransaction) o;
        return status == that.status
                && Objects.equals(transactionDate, that.transactionDate)
                && Objects.equals(explanation, that.explanation)
                && Objects.equals(amount, that.amount);
    }
}
