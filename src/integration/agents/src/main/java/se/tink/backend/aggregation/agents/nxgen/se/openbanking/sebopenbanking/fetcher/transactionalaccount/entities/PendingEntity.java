package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class PendingEntity {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDate;

    private String transactionId;
    private String creditorAccount;
    private String descriptiveText;
    private String pendingTypeDetailed;
    private String remittanceInformationUnstructured;
    private long remittanceInformationStructuredReference;
    private String ownNotes;
    private String creditorName;

    private TransactionAmountEntity transactionAmount;

    private String pendingType;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(getAmount())
                .setDate(valueDate)
                .setDescription(descriptiveText)
                .setPending(true)
                .build();
    }

    public UpcomingTransaction toTinkUpcomingTransaction() {
        return UpcomingTransaction.builder()
                .setDate(valueDate)
                .setAmount(transactionAmount.getAmount())
                .setDescription(creditorName)
                .build();
    }

    private ExactCurrencyAmount getAmount() {
        return isPendingTransaction()
                ? transactionAmount.getAmount().negate()
                : transactionAmount.getAmount();
    }

    public boolean isUpcomingTransaction() {
        return SebCommonConstants.TransactionType.UPCOMING.equalsIgnoreCase(pendingType);
    }

    public boolean isPendingTransaction() {
        return SebCommonConstants.TransactionType.RESERVED.equalsIgnoreCase(pendingType);
    }
}
