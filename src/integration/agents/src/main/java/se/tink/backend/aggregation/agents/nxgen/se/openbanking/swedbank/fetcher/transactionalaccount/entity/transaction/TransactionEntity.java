package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.Format;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.common.TransactionAmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {
    private Date valueDate;
    private Date transactionDate;
    private TransactionAmountEntity transactionAmount;
    private String remittanceInformationUnstructured;
    private String remittanceInformationStructured;

    public ExactCurrencyAmount getTransactionAmount() {
        return transactionAmount.toTinkAmount();
    }

    @JsonIgnore
    public Transaction toTinkTransaction(boolean isPending) {
        // Usually structured gives the type of transaction while unstructured gives more context
        String description =
                Strings.nullToEmpty(remittanceInformationStructured)
                        + Format.TRANSACTION_DESCRIPTION_SEPARATOR
                        + Strings.nullToEmpty(remittanceInformationUnstructured);
        return Transaction.builder()
                .setAmount(getTransactionAmount())
                .setDate(Optional.ofNullable(transactionDate).orElse(valueDate))
                .setDescription(description.trim())
                .setPending(isPending)
                .build();
    }
}
