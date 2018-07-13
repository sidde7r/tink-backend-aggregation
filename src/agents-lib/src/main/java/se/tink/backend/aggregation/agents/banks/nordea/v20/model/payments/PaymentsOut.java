package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentsOut {
    private final static TypeReference<List<PaymentEntity>> LIST_TYPE_REFERENCE =
            new TypeReference<List<PaymentEntity>>() {};
    private final static ObjectMapper MAPPER = new ObjectMapper();

    @JsonProperty("payment")
    private List<PaymentEntity> payments;

    public List<PaymentEntity> getPayments() {
        if (payments == null || payments.isEmpty()) {
            return Lists.newArrayList();
        }

        return Lists.newArrayList(FluentIterable
                .from(this.payments)
                .filter(Predicates.notNull()));
    }

    public List<PaymentEntity> getPayments(Payment.StatusCode statusCode) {
        List<PaymentEntity> payments = getPayments();

        if (payments.isEmpty() || statusCode == null) {
            return payments;
        }

        return Lists.newArrayList(FluentIterable
                .from(payments)
                .filter(statusCode.predicateForType()));
    }

    /**
     * Nordea API is a bit weird and send items on different formats depending on the number of items. Multiple
     * rows means that we will get an List of items and one row will not be typed as an array.
     */
    public void setPayments(Object input) {
        if (input instanceof Map) {
            this.payments = Lists.newArrayList(MAPPER.convertValue(input, PaymentEntity.class));
        } else {
            this.payments = MAPPER.convertValue(input, LIST_TYPE_REFERENCE);
        }
    }

}
