package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    private String transactionId;
    private String entryReference;
    private String endToEndId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDate;

    private AmountEntity transactionAmount;
    private String creditorName;
    private TransactionAccountInfoEntity creditorAccount;
    private String debtorName;
    private TransactionAccountInfoEntity debtorAccount;
    private String remittanceInformationUnstructured;
    private TransactionDetailsLinksEntity links;

    public Transaction toBookedTinkTransaction() {
        return toTinkTransaction(false);
    }

    public Transaction toPendingTinkTransaction() {
        return toTinkTransaction(true);
    }

    public Transaction toTinkTransaction(boolean isPending) {
        return Transaction.builder()
                .setAmount(transactionAmount.toAmount())
                .setDate(valueDate)
                .setDescription(remittanceInformationUnstructured)
                .setPending(isPending)
                .build();
    }
}
