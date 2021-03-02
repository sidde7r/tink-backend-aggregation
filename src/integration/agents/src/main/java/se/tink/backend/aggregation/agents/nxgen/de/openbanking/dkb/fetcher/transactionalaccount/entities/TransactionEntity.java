package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
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
                .setDescription(getDescription(transactionAmount))
                .setPending(false)
                .build();
    }

    @JsonIgnore
    public Transaction toPendingTinkTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.toAmount())
                .setDate(bookingDate)
                .setDescription(getDescription(transactionAmount))
                .setPending(true)
                .build();
    }

    private String getDescription(AmountEntity transactionAmount) {

        if (transactionAmount.toAmount().getExactValue().intValue() > 0) {
            return debtorName;
        } else if ((creditorName.toLowerCase().contains("paypal")
                        || creditorName.toLowerCase().contains("klarna"))
                && StringUtils.isNotEmpty(remittanceInformationUnstructured)) {
            return remittanceInformationUnstructured;
        }
        return creditorName;
    }
}
