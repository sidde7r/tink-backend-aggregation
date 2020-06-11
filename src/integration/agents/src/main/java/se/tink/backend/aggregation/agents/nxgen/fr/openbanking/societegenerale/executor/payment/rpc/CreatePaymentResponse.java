package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.PaymentRequestLinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@NoArgsConstructor
@AllArgsConstructor
@JsonObject
public class CreatePaymentResponse {

    @JsonProperty("_links")
    private PaymentRequestLinkEntity links;

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(PaymentType type) {
        return new PaymentResponse(
                new Payment.Builder().withUniqueId(getPaymentId()).withType(type).build());
    }

    public PaymentRequestLinkEntity getLinks() {
        return links;
    }

    private String getPaymentId() {
        String url = links.getConsentApproval().getUrl();
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public void setLinks(PaymentRequestLinkEntity links) {
        this.links = links;
    }
}
