package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingV31PaymentConstants.PaymentStatusCode;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@Data
public class DomesticPaymentConsentResponseData {

    private String status;
    private String consentId;
    private DomesticPaymentInitiation initiation;

    @JsonIgnore
    boolean hasStatusAwaitingAuthorisation() {
        return PaymentStatusCode.AWAITING_AUTHORISATION.equalsIgnoreCase(status);
    }
}
