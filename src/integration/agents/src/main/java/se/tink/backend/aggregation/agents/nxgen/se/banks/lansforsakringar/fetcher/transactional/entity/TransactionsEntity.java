package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction.Builder;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionsEntity {
    private String id;
    private String text;
    private AmountEntity amount;
    private String accountingDate;
    private String transactionDate;
    private String currencyDate;
    private String unconstrainedText;
    private double balance;
    private String marketingCode;
    private String transactionType;
    private boolean upcomingDeposit;

    public String getTransactionDate() {
        return transactionDate;
    }

    @JsonIgnore
    public Transaction toTinkTransaction() {

        Builder builder =
                Transaction.builder()
                        .setAmount(ExactCurrencyAmount.of(amount.getValue(), amount.getCurrency()))
                        .setDescription(unconstrainedText)
                        .setDate(LocalDate.parse(transactionDate));

        if (!Strings.isNullOrEmpty(id)) {
            builder.addExternalSystemIds(
                    TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID, id);
        }

        return builder.build();
    }
}
