package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.utils.berlingroup.AmountEntity;
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
    private String debtorName;
    private AccountEntity debtorAccount;
    private String endToEndId;
    private String purposeCode;

    @JsonIgnore
    public Transaction toBookedTinkTransaction() {
        return toTinkTransaction(false);
    }

    @JsonIgnore
    public Transaction toPendingTinkTransaction() {
        return toTinkTransaction(true);
    }

    private Transaction toTinkTransaction(boolean pending) {
        return Transaction.builder()
                .setDate(bookingDate)
                .setPending(pending)
                .setDescription(getDescription())
                .setAmount(transactionAmount.toTinkAmount())
                .build();
    }

    private String getDescription() {
        if (!isExpense()) {
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

    private boolean isExpense() {
        return transactionAmount.toTinkAmount().getExactValue().compareTo(BigDecimal.ZERO) < 0;
    }
}
