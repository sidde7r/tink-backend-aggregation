package se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    private Date bookingDate;
    private AccountEntity creditorAccount;
    private String creditorName;
    private String remittanceInformationUnstructured;
    private AmountEntity transactionAmount;
    private String transactionId;
    private String valueDate;

    @JsonIgnore
    public Transaction toBookedTinkTransaction() {
        return Transaction.builder()
                .setDate(bookingDate)
                .setPending(false)
                .setDescription(remittanceInformationUnstructured)
                .setAmount(transactionAmount.toAmount())
                .build();
    }

    @JsonIgnore
    public Transaction toPendingTinkTransaction() {
        return Transaction.builder()
                .setDate(bookingDate)
                .setPending(true)
                .setDescription(remittanceInformationUnstructured)
                .setAmount(transactionAmount.toAmount())
                .build();
    }
}
