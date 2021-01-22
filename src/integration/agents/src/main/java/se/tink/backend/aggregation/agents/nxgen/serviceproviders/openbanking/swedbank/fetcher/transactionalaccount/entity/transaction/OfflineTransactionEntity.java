package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class OfflineTransactionEntity extends TransactionEntity {
    @JsonFormat(pattern = "yyMMdd")
    private Date valueDate;

    @JsonFormat(pattern = "yyMMdd")
    private Date transactionDate;

    @JsonIgnore
    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setAmount(transactionAmount.toTinkAmount())
                .setDate(Optional.ofNullable(transactionDate).orElse(valueDate))
                .setDescription(
                        Optional.ofNullable(remittanceInformationUnstructured)
                                .orElse(remittanceInformationStructured))
                .setPending(false)
                .build();
    }
}
