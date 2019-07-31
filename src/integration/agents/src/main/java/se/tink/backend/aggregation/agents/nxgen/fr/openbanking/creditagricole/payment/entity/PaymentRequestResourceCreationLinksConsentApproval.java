package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentRequestResourceCreationLinksConsentApproval {
    @JsonProperty("href")
    private String href = null;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
