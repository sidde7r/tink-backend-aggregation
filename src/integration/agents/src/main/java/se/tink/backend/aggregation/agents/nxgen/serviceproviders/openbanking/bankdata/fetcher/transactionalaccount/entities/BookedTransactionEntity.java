package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class BookedTransactionEntity {

    private String transactionId;
    private BalanceAmountEntity transactionAmount;
    private RemittanceInformationStructuredEntity remittanceInformationStructured;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    @JsonIgnore
    private String getTransactionDescription() {
        if (remittanceInformationStructured == null
                || remittanceInformationStructured.getReference() == null) {
            return "";
        }
        return remittanceInformationStructured.getReference();
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setPending(false)
                .setAmount(transactionAmount.toAmount())
                .setDate(bookingDate)
                .setDescription(getTransactionDescription())
                .build();
    }
}
