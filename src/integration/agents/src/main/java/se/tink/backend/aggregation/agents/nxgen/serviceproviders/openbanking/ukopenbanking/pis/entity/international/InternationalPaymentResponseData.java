package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.international;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class InternationalPaymentResponseData {
    private String status;
    private String statusUpdateDateTime;
    private String creationDateTime;
    private String cutOffDateTime;
    private ExchangeRateInformationExtended exchangeRateInformation;
    private String consentId;
    private InternationalPaymentConsentInitiationRes initiation;

    public PaymentResponse toPaymentResponse() {
        return initiation.toPaymentResponse(status, consentId);
    }
}
