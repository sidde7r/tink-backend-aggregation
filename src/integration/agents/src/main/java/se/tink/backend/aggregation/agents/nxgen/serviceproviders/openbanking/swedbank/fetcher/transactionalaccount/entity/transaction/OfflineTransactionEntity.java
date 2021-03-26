package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import java.time.LocalDate;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class OfflineTransactionEntity extends TransactionEntity {

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyMMdd")
    private LocalDate valueDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyMMdd")
    private LocalDate transactionDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyMMdd")
    private LocalDate bookingDate;

    @JsonIgnore
    public Transaction toTinkTransaction(String providerMarket) {

        Builder builder =
                Transaction.builder()
                        .setAmount(transactionAmount.toTinkAmount())
                        .setDate(Optional.ofNullable(transactionDate).orElse(valueDate))
                        .setDescription(
                                Optional.ofNullable(remittanceInformationUnstructured)
                                        .orElse(remittanceInformationStructured))
                        .setPending(false)
                        .addTransactionDates(getTinkTransactionDates(valueDate, bookingDate))
                        .setProviderMarket(providerMarket);

        return (Transaction) builder.build();
    }
}
