package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.international;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class PaymentDataExtended {
    private String status;
    private String statusUpdateDateTime;
    private String creationDateTime;
    private String expectedExecutionDateTime;
    private ExchangeRateInformation exchangeRateInformation;
    private List<ChargesItem> charges;
    private String internationalPaymentId;
    private String consentId;
    private PaymentInitiationExtended initiation;
    private String expectedSettlementDateTime;
    private MultiAuthorisation multiAuthorisation;

    public PaymentResponse toTinkPaymentResponse() {
        return initiation.toTinkPaymentResponse(
                status, expectedExecutionDateTime, internationalPaymentId);
    }
}
