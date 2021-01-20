package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.common.TransactionAmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
    @JsonFormat(pattern = "yyMMdd")
    private Date valueDate;

    @JsonFormat(pattern = "yyMMdd")
    private Date transactionDate;

    private TransactionAmountEntity transactionAmount;
    private String remittanceInformationUnstructured;
    private String remittanceInformationStructured;

    @JsonIgnore
    public Transaction toTinkTransaction(boolean isPending) {

        return Transaction.builder()
                .setAmount(transactionAmount.toTinkAmount())
                .setDate(Optional.ofNullable(transactionDate).orElse(valueDate))
                .setDescription(
                        Optional.ofNullable(remittanceInformationUnstructured)
                                .orElse(remittanceInformationStructured))
                .setPending(isPending)
                .build();
    }
}
