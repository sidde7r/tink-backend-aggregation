package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;
import se.tink.libraries.amount.ExactCurrencyAmount;

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

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDate(getChargeDate())
                .setAmount(convertTransactionEntityToExactCurrencyAmount())
                .setDescription(description.replaceAll("\\s{2,}", " ")) // to remove whitespaces
                .setPending(checkIfPending())
                .build();
    }

    private boolean checkIfPending() {
        return postDate == null && referenceNumber == null;
    }

    private ExactCurrencyAmount convertTransactionEntityToExactCurrencyAmount() {
        return new ExactCurrencyAmount(amount, isoAlphaCurrencyCode).negate();
    }
}
