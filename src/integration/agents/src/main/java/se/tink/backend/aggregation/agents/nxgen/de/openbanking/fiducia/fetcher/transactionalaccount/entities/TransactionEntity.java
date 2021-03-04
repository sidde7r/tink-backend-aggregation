package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.entities;

import java.math.BigDecimal;
import java.util.Date;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.agents.utils.berlingroup.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionEntity {

    private Date bookingDate;
    private String remittanceInformationUnstructured;
    private AmountEntity transactionAmount;
    private String debtorName;
    private String creditorName;

    Transaction toBookedTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.toTinkAmount())
                .setDate(bookingDate)
                .setPending(false)
                .setDescription(getDescription())
                .build();
    }

    public String getDescription() {

        if (transactionAmount.toTinkAmount().getExactValue().compareTo(BigDecimal.ZERO) > 0) {
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
