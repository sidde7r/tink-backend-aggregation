package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import lombok.Data;
import se.tink.backend.aggregation.agents.models.TransactionDateType;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDate;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
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

    public Transaction toTinkTransaction(String providerMarket) {
        Builder builder =
                Transaction.builder()
                        .setAmount(convertTransactionEntityToExactCurrencyAmount())
                        .setDescription(
                                description.replaceAll("\\s{2,}", " ")) // to remove whitespaces
                        .setPending(isPending())
                        .setDate(chargeDate)
                        .addTransactionDates(getTransactionDates())
                        .setProprietaryFinancialInstitutionType(type)
                        .setTransactionReference(referenceNumber)
                        .setProviderMarket(providerMarket)
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                identifier);

        if (extendedDetails != null && extendedDetails.getMerchant() != null) {
            builder.setMerchantName(extendedDetails.getMerchant().getName());
        }

        return (Transaction) builder.build();
    }

    /** From documentation: Pending transactions will not have values for post_date */
    private boolean isPending() {
        return postDate == null;
    }

    private ArrayList<TransactionDate> getTransactionDates() {
        ArrayList<TransactionDate> transactionDates = new ArrayList<>();

        AvailableDateInformation valueDateInformation = new AvailableDateInformation();
        valueDateInformation.setDate(chargeDate);

        transactionDates.add(
                TransactionDate.builder()
                        .type(TransactionDateType.VALUE_DATE)
                        .value(valueDateInformation)
                        .build());

        if (postDate != null) {
            AvailableDateInformation bookingDateInformation = new AvailableDateInformation();
            bookingDateInformation.setDate(postDate);
            transactionDates.add(
                    TransactionDate.builder()
                            .type(TransactionDateType.BOOKING_DATE)
                            .value(bookingDateInformation)
                            .build());
        }

        return transactionDates;
    }

    private ExactCurrencyAmount convertTransactionEntityToExactCurrencyAmount() {
        return new ExactCurrencyAmount(amount, isoAlphaCurrencyCode).negate();
    }
}
