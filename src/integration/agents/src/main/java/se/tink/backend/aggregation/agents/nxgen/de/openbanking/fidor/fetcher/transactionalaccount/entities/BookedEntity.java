package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class BookedEntity {
    private String transactionId;
    private String creditorName;
    private TransactionAccountEntity creditorAccount;
    private String debtorName;
    private TransactionAccountEntity debtorAccount;
    private BalanceAmountEntity transactionAmount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    private String remittanceInformationUnstructured;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.toAmount())
                .setDate(valueDate)
                .setDescription(creditorName)
                .setPending(false)
                .build();
    }
}
