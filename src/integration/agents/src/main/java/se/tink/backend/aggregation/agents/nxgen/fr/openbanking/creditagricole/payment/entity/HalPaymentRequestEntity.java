package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HalPaymentRequestEntity {
    @JsonProperty("paymentRequest")
    private PaymentRequestResourceEntity paymentRequest = null;

    @JsonProperty("_links")
    private PaymentRequestLinksEntity links = null;

    public PaymentRequestResourceEntity getPaymentRequest() {
        return paymentRequest;
    }

    public void setPaymentRequest(PaymentRequestResourceEntity paymentRequest) {
        this.paymentRequest = paymentRequest;
    }

    public PaymentRequestLinksEntity getLinks() {
        return links;
    }

    public void setLinks(PaymentRequestLinksEntity links) {
        this.links = links;
    }
}
