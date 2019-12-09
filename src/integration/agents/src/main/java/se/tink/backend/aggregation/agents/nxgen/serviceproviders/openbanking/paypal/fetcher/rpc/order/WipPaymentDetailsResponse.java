package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.rpc.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.ExceptionMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.LinkTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.RequestConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalUtil;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.executor.payment.enums.PayPalPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.payment.order.PurchaseUnitsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.shared.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;

@JsonObject
public class WipPaymentDetailsResponse {
    private String id;

    @JsonProperty("purchase_units")
    private List<PurchaseUnitsEntity> purchaseUnits;

    private List<LinkEntity> links;
    private String status;

    private Debtor toTinkDebtor() {
        return Optional.ofNullable(purchaseUnits)
                .flatMap(list -> list.stream().findFirst())
                .map(purchase -> purchase.getPayee().toTinkDebtor())
                .orElse(null);
    }

    private Reference toTinkReference() {
        LinkEntity link =
                PayPalUtil.findByRelation(links, LinkTypes.APPROVE)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                String.format(
                                                        ExceptionMessages.NO_LINK_WITH_TYPE,
                                                        LinkTypes.APPROVE)));

        return new Reference(RequestConstants.REFERENCE_TYPE, link.getReference());
    }

    public PaymentResponse toTinkPayment() {
        PayPalPaymentStatus paymentStatus = PayPalPaymentStatus.fromString(status);
        Payment.Builder builder =
                new Payment.Builder()
                        .withUniqueId(id)
                        .withStatus(PayPalPaymentStatus.mapToTinkPaymentStatus(paymentStatus))
                        .withDebtor(toTinkDebtor())
                        .withReference(toTinkReference());
        return new PaymentResponse(builder.build());
    }

    public PaymentResponse toTinkPaymentApprove() {
        PayPalPaymentStatus paymentStatus = PayPalPaymentStatus.fromString(status);
        Payment.Builder builder =
                new Payment.Builder()
                        .withUniqueId(id)
                        .withStatus(PayPalPaymentStatus.mapToTinkPaymentStatus(paymentStatus))
                        .withDebtor(toTinkDebtor());
        return new PaymentResponse(builder.build());
    }
}
