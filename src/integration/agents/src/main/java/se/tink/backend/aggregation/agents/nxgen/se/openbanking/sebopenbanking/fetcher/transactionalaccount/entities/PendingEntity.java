package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

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
                .setAmount(transactionAmount.getAmount())
                .setDate(valueDate)
                .setDescription(creditorName)
                .setPending(true)
                .build();
    }
}
