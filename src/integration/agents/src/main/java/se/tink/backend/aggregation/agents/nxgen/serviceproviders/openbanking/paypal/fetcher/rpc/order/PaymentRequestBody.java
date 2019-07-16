package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.rpc.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.RequestConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.payment.order.PurchaseUnitsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class PaymentRequestBody {

    private String intent;

    @JsonProperty("purchase_units")
    private List<PurchaseUnitsEntity> purchases;

    public PaymentRequestBody() {}

    private PaymentRequestBody(Builder builder) {
        this.intent = builder.intent;
        this.purchases = builder.purchases;
    }

    public String getIntent() {
        return intent;
    }

    public List<PurchaseUnitsEntity> getPurchases() {
        return purchases;
    }

    public static PaymentRequestBody of(PaymentRequest paymentRequest) {
        List<PurchaseUnitsEntity> purchases =
                Stream.of(paymentRequest).map(PurchaseUnitsEntity::of).collect(Collectors.toList());
        return new Builder().withIntent(RequestConstants.CAPTURE).withPurchase(purchases).build();
    }

    public static class Builder {
        private String intent;

        private List<PurchaseUnitsEntity> purchases;

        public Builder withIntent(String intent) {
            this.intent = intent;
            return this;
        }

        public Builder withPurchase(List<PurchaseUnitsEntity> purchases) {
            this.purchases = purchases;
            return this;
        }

        public PaymentRequestBody build() {
            return new PaymentRequestBody(this);
        }
    }
}
