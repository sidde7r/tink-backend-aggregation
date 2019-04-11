package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionsEntity {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    private double amount;
    private String receiverOrPayerName;
    // `reference` is null - cannot define it!
    private String message;
    private String transactionId;
    private String transactionType;

    @JsonIgnore
    private String getDescription() {
        if (!Strings.isNullOrEmpty(message)) {
            return message;
        }

        return transactionType;
    }

    @JsonIgnore
    public Transaction toTinkTransaction() {
        // Note that Aktia does not specify currency. All amounts are in EUR.
        return Transaction.builder()
                .setDate(bookingDate)
                .setDescription(getDescription())
                .setAmount(Amount.inEUR(amount))
                .build();
    }
}
