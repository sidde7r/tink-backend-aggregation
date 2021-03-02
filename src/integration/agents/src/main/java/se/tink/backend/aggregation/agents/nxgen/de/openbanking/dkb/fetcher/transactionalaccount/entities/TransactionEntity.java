package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
    private Date bookingDate;
    private String remittanceInformationUnstructured;
    private AmountEntity transactionAmount;
    private String creditorName;
    private String debtorName;

    @JsonIgnore
    public Transaction toBookedTinkTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.toAmount())
                .setDate(bookingDate)
                .setDescription(setDescription(transactionAmount))
                .setPending(false)
                .build();
    }

    @JsonIgnore
    public Transaction toPendingTinkTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.toAmount())
                .setDate(bookingDate)
                .setDescription(setDescription(transactionAmount))
                .setPending(true)
                .build();
    }

    private String setDescription(AmountEntity transactionAmount) {

        if (transactionAmount.toAmount().getExactValue().intValue() > 0) {
            return debtorName;
        } else if ((creditorName.toLowerCase().contains("paypal")
                        || creditorName.toLowerCase().contains("klarna"))
                && !remittanceInformationUnstructured.isEmpty()) {
            return remittanceInformationUnstructured;
        }
        return creditorName;
    }
}
