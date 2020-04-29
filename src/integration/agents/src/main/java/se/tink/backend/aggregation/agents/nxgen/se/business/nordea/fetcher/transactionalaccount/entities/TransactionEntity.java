package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {
    private String fromProductIdentity;
    private String transactionText;
    private String transactionKey;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date transactionDate;

    private String transactionFromAccountNumber;
    private String transactionToAccountNumber;
    private boolean isCoverReservationTransaction;
    private boolean fileImport;
    private TransactionAmountEntity transactionAmount;

    public String getTransactionKey() {
        return transactionKey;
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(
                        ExactCurrencyAmount.of(
                                transactionAmount.getAmount(), transactionAmount.getCurrency()))
                .setDate(transactionDate)
                .setDescription(transactionText)
                .setPending(isCoverReservationTransaction)
                .build();
    }
}
