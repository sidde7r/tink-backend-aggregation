package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class BookedPendingTransactionEntity {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    private String creditorId;
    private String creditorName;
    private String debtorName;
    private String mandateId;
    private String remittanceInformationUnstructured;
    private BalanceAmountEntity transactionAmount;
    private String transactionId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDate;

    public Transaction toTinkTransaction(boolean pending) {
        return Transaction.builder()
                .setPending(pending)
                .setAmount(transactionAmount.toAmount())
                .setDate(bookingDate)
                .setDescription(remittanceInformationUnstructured)
                .build();
    }
}
