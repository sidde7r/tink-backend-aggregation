package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
    private String bankTransactionCode;
    private String checkId;
    private String transactionId;
    private TransactionAmountEntity transactionAmount;
    private String remittanceInformationUnstructured;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    public Transaction toTinkTransaction(boolean pending) {
        return Transaction.builder()
                .setPending(pending)
                .setAmount(transactionAmount.toAmount())
                .setDate(bookingDate)
                .setDescription(remittanceInformationUnstructured)
                .build();
    }
}
