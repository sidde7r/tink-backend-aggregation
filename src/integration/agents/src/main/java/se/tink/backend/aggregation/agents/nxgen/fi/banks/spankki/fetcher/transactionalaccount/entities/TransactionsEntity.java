package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionsEntity {
    private String transactionId;

    @JsonFormat(pattern = "yyyyMMdd")
    private Date dueDate;

    private String bookingDate;
    private double amount;
    private String currency;
    private String originalAmount;
    private String originalCurrency;
    private String recieverName;
    private String reference;
    private String textCode;
    private boolean ownRegistered;
    private boolean copyable;
    private boolean sepaTransaction;
    private String periodicity;
    private String endDate;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(new Amount(currency, amount))
                .setDate(dueDate)
                .setDescription(recieverName)
                .build();
    }
}
