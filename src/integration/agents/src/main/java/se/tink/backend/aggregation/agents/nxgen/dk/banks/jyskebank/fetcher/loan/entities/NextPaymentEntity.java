package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.loan.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class NextPaymentEntity {
    private String paymentDate;
    private String period;
    private boolean instalmentFree;
    private String paymentAfterTax;
    private String paymentBeforeTax;
    private String interest;
    private String repayment;
    private String administrationMargin;
}
