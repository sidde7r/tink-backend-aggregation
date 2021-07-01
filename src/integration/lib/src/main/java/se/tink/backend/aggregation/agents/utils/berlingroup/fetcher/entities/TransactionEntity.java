package se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities;

import java.time.LocalDate;
import lombok.Getter;
import se.tink.backend.aggregation.agents.utils.berlingroup.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TransactionEntity {

    private String transactionId;
    private LocalDate bookingDate;
    private LocalDate valueDate;
    private AmountEntity transactionAmount;
    private String creditorName;
    private String debtorName;
    private String remittanceInformationUnstructured;
}
