package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.entities.payments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21Constants;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentList {
    private static final TypeReference<List<PaymentEntity>> LIST_TYPE_REFERENCE =
            new TypeReference<List<PaymentEntity>>() {};
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonProperty("payment")
    private List<PaymentEntity> payments;

    public List<PaymentEntity> getPayments() {
        if (payments == null || payments.isEmpty()) {
            return Lists.newArrayList();
        }

        return this.payments.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<PaymentEntity> getPayments(NordeaV21Constants.Payment.StatusCode statusCode) {
        List<PaymentEntity> payments = getPayments();

        if (payments.isEmpty() || statusCode == null) {
            return payments;
        }

        return payments.stream().filter(statusCode.predicateForType()).collect(Collectors.toList());
    }

    /**
     * Nordea API is a bit weird and send items on different formats depending on the number of
     * items. Multiple rows means that we will get an List of items and one row will not be typed as
     * an array.
     */
    public void setPayments(Object input) {
        if (input instanceof Map) {
            this.payments = Lists.newArrayList(MAPPER.convertValue(input, PaymentEntity.class));
        } else {
            this.payments = MAPPER.convertValue(input, LIST_TYPE_REFERENCE);
        }
    }
}
