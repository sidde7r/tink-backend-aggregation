package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
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

    /** Date transaction was booked to the account ledger */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate bookingDate;

    /** Value date of the transaction, used for interest calculation */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate valueDate;

    /** Date on which transaction was enacted */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate transactionDate;

    /** Payment date of the transaction */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate paymentDate;

    private BigDecimal amount;
    private String cardNumber;
    private String counterpartyAccount;
    private String counterpartyName;
    private String currency;
    private String currencyRate;
    private String message;
    private String narrative;
    private String originalCurrency;
    private String originalCurrencyAmount;
    private String ownMessage;
    private String reference;
    private String status;
    private String transactionId;
    private String typeDescription;

    public String getCounterpartyName() {
        return counterpartyName;
    }

    public String getNarrative() {
        return narrative;
    }

    public String getTypeDescription() {
        return typeDescription;
    }

    public Transaction toTinkTransaction(String providerMarket) {

        LocalDate trxDate = bookingDate != null ? bookingDate : transactionDate;

        Builder builder =
                Transaction.builder()
                        .setAmount(getAmount())
                        .setDate(trxDate)
                        .setDescription(getDescription())
                        .setPending(isPending())
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                transactionId)
                        .setTransactionDates(getTinkTransactionDates())
                        .setProprietaryFinancialInstitutionType(typeDescription)
                        .setProviderMarket(providerMarket);

        if (Objects.nonNull(reference)) {
            builder.setTransactionReference(reference);
        }

        return (Transaction) builder.build();
    }

    private TransactionDates getTinkTransactionDates() {
        TransactionDates.Builder builder = TransactionDates.builder();

        if (Objects.nonNull(valueDate)) {
            builder.setValueDate(new AvailableDateInformation().setDate(valueDate));
        }

        if (Objects.nonNull(bookingDate)) {
            builder.setBookingDate(new AvailableDateInformation().setDate(bookingDate));
        }

        return builder.build();
    }

    public String getDescription() {
        return (narrative != null) ? narrative : typeDescription;
    }

    private ExactCurrencyAmount getAmount() {
        return new ExactCurrencyAmount(amount, currency);
    }

    private boolean isPending() {
        return status.equalsIgnoreCase(NordeaBaseConstants.StatusResponse.RESERVED);
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public LocalDate getValueDate() {
        return valueDate;
    }
}
