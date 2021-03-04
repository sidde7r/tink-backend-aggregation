package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
    protected Date bookingDate;
    protected String remittanceInformationUnstructured;
    protected AmountEntity transactionAmount;
    protected String creditorName;
    protected String debtorName;

    @JsonIgnore
    public Transaction toBookedTinkTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.toAmount())
                .setDate(bookingDate)
                .setDescription(getDescription())
                .setPending(false)
                .build();
    }

    @JsonIgnore
    public Transaction toPendingTinkTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.toAmount())
                .setDate(bookingDate)
                .setDescription(getDescription())
                .setPending(true)
                .build();
    }

    private String getDescription() {
        if (transactionAmount.toAmount().getExactValue().compareTo(BigDecimal.ZERO) > 0) {
            if (StringUtils.isNotEmpty(debtorName)) {
                return debtorName;
            }
        } else if (StringUtils.isNotEmpty(creditorName)) {
            if ((creditorName.toLowerCase().contains("paypal")
                            || creditorName.toLowerCase().contains("klarna"))
                    && StringUtils.isNotEmpty(remittanceInformationUnstructured)) {
                return remittanceInformationUnstructured;
            } else {
                return creditorName;
            }
        }
        return remittanceInformationUnstructured;
    }
}
