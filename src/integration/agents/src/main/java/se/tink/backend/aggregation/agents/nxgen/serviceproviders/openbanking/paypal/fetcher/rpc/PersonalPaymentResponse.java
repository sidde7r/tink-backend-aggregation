package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.rpc;

import java.util.List;
import java.util.Optional;
import org.codehaus.jackson.annotate.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.executor.payment.enums.PayPalPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.payment.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.payment.PayeeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.shared.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment.Builder;
import se.tink.libraries.payment.rpc.Reference;

@JsonObject
public class PersonalPaymentResponse {

    private String id;

    private AmountEntity amount;

    private PayeeEntity payee;

    @JsonProperty("payment_type")
    private String paymentType;

    private List<LinkEntity> links;

    private String status;

    public String getId() {
        return id;
    }

    public AmountEntity getAmount() {
        return amount;
    }

    public PayeeEntity getPayee() {
        return payee;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public List<LinkEntity> getLinks() {
        return links;
    }

    public String getStatus() {
        return status;
    }

    private Reference toTinkReference() {
        return Optional.ofNullable(links)
                .flatMap(list -> list.stream().findFirst())
                .map(link -> new Reference(link.getRelation(), link.getReference()))
                .orElse(null);
    }

    public PaymentResponse toTinkResponse() {
        PayPalPaymentStatus payPalPaymentStatus = PayPalPaymentStatus.fromString(status);
        return new PaymentResponse(
                new Builder()
                        .withUniqueId(id)
                        .withCurrency(amount.getCurrency())
                        .withAmount(amount.toTinkAmount())
                        .withDebtor(payee.toTinkDebtor())
                        .withStatus(PayPalPaymentStatus.mapToTinkPaymentStatus(payPalPaymentStatus))
                        .withType(PaymentType.INTERNATIONAL)
                        .withReference(toTinkReference())
                        .build());
    }
}
