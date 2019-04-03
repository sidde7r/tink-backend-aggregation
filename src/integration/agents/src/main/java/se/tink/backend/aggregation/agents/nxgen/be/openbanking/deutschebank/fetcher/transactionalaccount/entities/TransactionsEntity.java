package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionsEntity {

    private String originIban;

    private Number amount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    private String currencyCode;

    private String transactionCode;

    private String counterPartyName;

    private String paymentReference;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDate;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
            .setAmount(getAmount())
            .setDate(bookingDate)
            .setDescription(getDescription())
            .setPending(false)
            .build();
    }

    private Amount getAmount() {
        return new Amount(currencyCode, amount);
    }

    private String getDescription() {
        return Strings.isNullOrEmpty(counterPartyName)
            ? Strings.isNullOrEmpty(paymentReference) ? "" : paymentReference
            : Strings.isNullOrEmpty(paymentReference)
                ? counterPartyName
                : counterPartyName + " " + paymentReference;
    }
}
