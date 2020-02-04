package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount.entities;

import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class BookedEntity {

    private String bookingDate;
    private DebtorAccountEntity debtorAccount;
    private String entryReference;
    private String remittanceInformationUnstructured;
    private BalanceAmountEntity transactionAmount;
    private Date transactionDate;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.toAmount())
                .setDate(transactionDate)
                .setDescription(remittanceInformationUnstructured)
                .setPending(false)
                .build();
    }
}
