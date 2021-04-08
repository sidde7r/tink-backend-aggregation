package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@Slf4j
public class TransactionDto {

    private String identifier;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate chargeDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate postDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate statementEndDate;

    private BigDecimal amount;

    private String referenceNumber;

    private String type;

    private String description;

    private String isoAlphaCurrencyCode;

    private String displayAccountNumber;

    private String firstName;

    private String lastName;

    private String embossedName;

    private ExtendedDetailsDto extendedDetails;

    private Object foreignDetails;

    public Optional<Transaction> toTinkTransaction(String providerMarket) {

        Optional<ExactCurrencyAmount> tinkAmount = getTinkTransactionAmount();
        if (!tinkAmount.isPresent()) {
            return Optional.empty();
        }

        Builder builder =
                Transaction.builder()
                        .setAmount(tinkAmount.get())
                        .setDescription(
                                description.replaceAll("\\s{2,}", " ")) // to remove whitespaces
                        .setPending(isPending())
                        .setDate(chargeDate)
                        .setTransactionDates(getTransactionDates())
                        .setProprietaryFinancialInstitutionType(type)
                        .setTransactionReference(referenceNumber)
                        .setProviderMarket(providerMarket)
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                identifier);

        if (extendedDetails != null && extendedDetails.getMerchant() != null) {
            builder.setMerchantName(extendedDetails.getMerchant().getName());
        }

        return Optional.of((Transaction) builder.build());
    }

    /**
     * Pending foreign transactions does not have an amount. Assumption is that the transaction will
     * have an amount when the transaction is booked. Won't parse amount from foreign details even
     * if foreign amount and foreign currency is present as there's a risk that deduplication won't
     * work and user will have two transactions; one in local and one in foreign currency. Amex
     * support has been contacted about this since amount is a required field: TC-4417.
     */
    private Optional<ExactCurrencyAmount> getTinkTransactionAmount() {
        if (Objects.nonNull((amount))) {
            return Optional.of(new ExactCurrencyAmount(amount, isoAlphaCurrencyCode).negate());
        }

        if (Objects.nonNull(foreignDetails)) {
            log.info("Foreign transaction without amount. Transaction will be ignored.");
            return Optional.empty();
        }

        log.warn(
                "Transaction has no amount or foreign details, needs to be investigated. Transaction will be ignored.");
        return Optional.empty();
    }

    /** From documentation: Pending transactions will not have values for post_date */
    private boolean isPending() {
        return postDate == null;
    }

    private TransactionDates getTransactionDates() {
        TransactionDates.Builder builder = TransactionDates.builder();

        builder.setValueDate(new AvailableDateInformation().setDate(chargeDate));

        if (postDate != null) {
            builder.setBookingDate(new AvailableDateInformation().setDate(postDate));
        }

        return builder.build();
    }
}
