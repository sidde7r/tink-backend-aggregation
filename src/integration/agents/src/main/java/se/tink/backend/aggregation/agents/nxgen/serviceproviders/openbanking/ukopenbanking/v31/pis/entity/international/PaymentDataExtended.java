package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.international;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Payment;

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

    public Payment toTinkPayment() {
        return initiation.toTinkPayment(status, expectedExecutionDateTime, internationalPaymentId);
    }
}
