package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Payment.Builder;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class DomesticPaymentConsentResponseData {
    private String status;
    private String statusUpdateDateTime;
    private String creationDateTime;
    private String expectedExecutionDateTime;
    private String cutOffDateTime;
    private Authorisation authorisation;
    private List<ChargesItem> charges;
    private String consentId;
    private Initiation initiation;
    private String expectedSettlementDateTime;

    private PaymentStatus getPaymentStatus() {
        return UkOpenBankingV31Constants.toPaymentStatus(status);
    }

    public PaymentResponse toTinkPaymentResponse() {
        Payment payment =
                new Builder()
                        .withAmount(initiation.toTinkAmount())
                        .withStatus(getPaymentStatus())
                        .withDebtor(initiation.getDebtor())
                        .withCreditor(initiation.getCreditor())
                        .withCurrency(initiation.toTinkAmount().getCurrency())
                        .withReference(initiation.getReference())
                        .build();

        Storage storage = new Storage();
        storage.put(UkOpenBankingV31Constants.Storage.CONSENT_ID, consentId);

        return new PaymentResponse(payment, storage);
    }

    public String getConsentId() {
        return consentId;
    }
}
