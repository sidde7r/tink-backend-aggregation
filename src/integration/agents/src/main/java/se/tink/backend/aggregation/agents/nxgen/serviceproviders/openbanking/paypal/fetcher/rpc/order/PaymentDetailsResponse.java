package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.rpc.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.RequestConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.executor.payment.enums.PayPalOrderPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.payment.order.PurchaseUnitsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.shared.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class PaymentDetailsResponse {

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
        return new Reference(
                RequestConstants.REFERENCE_TYPE, SerializationUtils.serializeToString(links));
    }

    @JsonIgnore
    public PaymentResponse toTinkPayment() {
        PayPalOrderPaymentStatus paymentStatus = PayPalOrderPaymentStatus.fromString(status);
        Payment.Builder builder =
                new Payment.Builder()
                        .withUniqueId(id)
                        .withStatus(PayPalOrderPaymentStatus.mapToTinkPaymentStatus(paymentStatus))
                        .withDebtor(toTinkDebtor())
                        .withReference(toTinkReference());
        return new PaymentResponse(builder.build());
    }
}
