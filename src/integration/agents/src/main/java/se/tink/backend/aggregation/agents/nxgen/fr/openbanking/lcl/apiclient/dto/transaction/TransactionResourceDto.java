package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction;

import java.time.LocalDate;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.common.AmountDto;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class TransactionResourceDto {

    private BankTransactionCodeDto bankTransactionCode;

    private LocalDate bookingDate;

    private ChargesResourceDto charges;

    private CreditDebitIndicator creditDebitIndicator;

    private String entryReference;

    private LocalDate expectingBookingDate;

    private RemittanceInformationDto remittanceInformation;

    private String resourceId;

    private TransactionStatus status;

    private AmountDto transactionAmount;

    private LocalDate transactionDate;

    private LocalDate valueDate;
}
