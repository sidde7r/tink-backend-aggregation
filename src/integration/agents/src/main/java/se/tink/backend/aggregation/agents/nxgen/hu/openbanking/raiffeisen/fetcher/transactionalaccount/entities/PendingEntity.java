package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.fetcher.transactionalaccount.entities;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount.entities.BalanceAmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class PendingEntity {
    private Date bookingDate;
    private String purposeCode;
    private String remittanceInformationStructured;
    private String remittanceInformationUnstructured;
    private BalanceAmountEntity transactionAmount;
    private String transactionId;
    private String ultimateCreditor;
    private String ultimateDebtor;
    private String valueDate;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.toAmount())
                .setDate(bookingDate)
                .setPending(true)
                .build();
    }
}
