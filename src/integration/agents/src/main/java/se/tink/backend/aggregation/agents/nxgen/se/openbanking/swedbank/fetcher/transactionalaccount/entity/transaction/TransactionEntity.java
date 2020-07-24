package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.Format;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {
    private String transactionDate;
    private String transactionAmount;
    private String remittanceInformationStructured;

    @JsonIgnore
    public AggregationTransaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(
                        ExactCurrencyAmount.of(
                                transactionAmount, SwedbankConstants.Transaction.CURRENCY))
                .setDate(
                        LocalDate.parse(
                                transactionDate,
                                DateTimeFormatter.ofPattern(Format.TRANSACTION_DATE_FORMAT)))
                .setDescription(remittanceInformationStructured)
                .build();
    }
}
