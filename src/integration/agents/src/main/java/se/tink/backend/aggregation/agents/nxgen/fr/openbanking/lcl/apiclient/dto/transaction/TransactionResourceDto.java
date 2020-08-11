package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.common.AmountDto;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDMYFormatDeserializer;

@JsonObject
@Data
public class TransactionResourceDto {

    private BankTransactionCodeDto bankTransactionCode;

    @JsonDeserialize(using = LocalDateDMYFormatDeserializer.class)
    private LocalDate bookingDate;

    private ChargesResourceDto charges;

    private CreditDebitIndicator creditDebitIndicator;

    private String entryReference;

    @JsonDeserialize(using = LocalDateDMYFormatDeserializer.class)
    private LocalDate expectingBookingDate;

    private RemittanceInformationDto remittanceInformation;

    private String resourceId;

    private TransactionStatus status;

    private AmountDto transactionAmount;

    @JsonDeserialize(using = LocalDateDMYFormatDeserializer.class)
    private LocalDate transactionDate;

    @JsonDeserialize(using = LocalDateDMYFormatDeserializer.class)
    private LocalDate valueDate;
}
