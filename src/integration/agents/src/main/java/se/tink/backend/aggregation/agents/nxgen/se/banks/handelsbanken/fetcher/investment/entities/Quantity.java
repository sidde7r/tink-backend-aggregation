package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.deserializers.DoubleDeserializer;

@JsonObject
public class Quantity {

    @JsonDeserialize(using = DoubleDeserializer.class)
    private Double quantityFormatted;

    public Optional<Double> asDouble() {
        return Optional.ofNullable(quantityFormatted);
    }

    public boolean hasNoValue() {
        return quantityFormatted == null || quantityFormatted == 0d;
    }

    @VisibleForTesting
    void setQuantityFormatted(Double quantityFormatted) {
        this.quantityFormatted = quantityFormatted;
    }
}
