package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.deserializers.DoubleDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class HandelsbankenAmount {

    private String currency;
    private Double amount;

    @JsonDeserialize(using = DoubleDeserializer.class)
    private Double amountFormatted;

    private String unit;

    public Double asDouble() {
        return amount != null ? amount : amountFormatted;
    }

    public static Double asDoubleOrElseNull(HandelsbankenAmount amount) {
        return Optional.ofNullable(amount).map(HandelsbankenAmount::asDouble).orElse(null);
    }

    public Double getAmount() {
        return amount;
    }

    public Double getAmountFormatted() {
        return amountFormatted;
    }

    public String getUnit() {
        return unit;
    }

    @VisibleForTesting
    public HandelsbankenAmount setAmount(Double amount) {
        this.amount = amount;
        return this;
    }

    @VisibleForTesting
    public HandelsbankenAmount setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public String getCurrency() {
        return currency;
    }

    @JsonIgnore
    public ExactCurrencyAmount toExactCurrencyAmount() {
        if (Objects.isNull(amount)) {
            return ExactCurrencyAmount.of(amountFormatted, currency);
        }

        return ExactCurrencyAmount.of(amount, currency);
    }
}
