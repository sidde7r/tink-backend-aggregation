package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;

@JsonObject
@Getter
public class TransactionEntity {

    private String resourceId;
    private String bookingDate;
    private String transactionDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate valueDate;

    private String creditDebitIndicator;
    private String entryReference;
    private List<String> remittanceInformation;
    private String status;
    private TransactionAmount transactionAmount;

    public LocalDate getBookingDate() {
        return LocalDate.parse(bookingDate);
    }
}
