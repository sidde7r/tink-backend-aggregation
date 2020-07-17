package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class PendingEntity {

    private String bookingDate;
    private DebtorAccountEntity debtorAccount;
    private String entryReference;
    private String remittanceInformationUnstructured;
    private BalanceAmountEntity transactionAmount;
    private Date transactionDate;
    private String merchantName;
    private String text;

    @JsonIgnore
    private String getDescription() {
        if (remittanceInformationUnstructured != null) {
            return remittanceInformationUnstructured;
        }
        return merchantName != null ? merchantName : text;
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.getAmount())
                .setDate(transactionDate)
                .setDescription(getDescription())
                .setPending(true)
                .build();
    }
}
