package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TransactionEntity {

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate accountingDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate valueDate;

    private Boolean recurringTransfer;
    private String transferId;
    private BigDecimal amount;
    private String statement;
    private String counterPartStatement;
    private String type;
    private String status;
    private String currency;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        Builder builder =
                Transaction.builder()
                        .setDate(getTransactionDate())
                        .setAmount(ExactCurrencyAmount.of(amount, currency))
                        .setDescription(getStatement())
                        .setPending(isPending())
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                transferId)
                        .setTransactionDates(getTinkTransactionDates())
                        .setProprietaryFinancialInstitutionType(type)
                        .setProviderMarket(SbabConstants.PROVIDER_MARKET);

        return (Transaction) builder.build();
    }

    private TransactionDates getTinkTransactionDates() {
        TransactionDates.Builder builder = TransactionDates.builder();

        builder.setValueDate(new AvailableDateInformation().setDate(valueDate));

        if (Objects.nonNull(accountingDate)) {
            builder.setBookingDate(new AvailableDateInformation().setDate(accountingDate));
        }

        return builder.build();
    }

    private LocalDate getTransactionDate() {
        return Optional.ofNullable(accountingDate).orElse(valueDate);
    }

    private String getStatement() {
        return Optional.ofNullable(statement).orElse(counterPartStatement);
    }

    public Boolean isPending() {
        return "pending".equals(status);
    }
}
