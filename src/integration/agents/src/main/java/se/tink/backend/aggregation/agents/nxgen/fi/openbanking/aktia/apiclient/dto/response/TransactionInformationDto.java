package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class TransactionInformationDto {

    private BigDecimal amount;

    private LocalDate bookingDate;

    private String message;

    private String receiverOrPayerName;

    private String reference;

    private String transactionId;

    private String transactionType;
}
