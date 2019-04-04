package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    private String transactionId;
    private String endToEndId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date executionDateTime;
    private TransactionAmountEntity transactionAmount;
    private String creditorName;
    private TransactionAccountEntity creditorAccount;
    private String debtorName;
    private TransactionAccountEntity debtorAccount;
    private String transactionType;
    private String remittanceInformationUnstructured;
    private RemittanceInformationEntity remittanceInformationStructured;

    public Transaction toBookedTinkTransaction() {
        return toTinkTransaction(false);
    }

    public Transaction toPendingTinkTransaction() {
        return toTinkTransaction(true);
    }

    private Transaction toTinkTransaction(boolean isPending) {
        return Transaction.builder()
            .setPending(isPending)
            .setExternalId(transactionId)
            .setDescription(remittanceInformationUnstructured)
            .setAmount(transactionAmount.toAmount())
            .setDate(bookingDate)
            .build();
    }
}
