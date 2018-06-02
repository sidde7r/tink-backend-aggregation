package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.protostuff.Tag;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.Embeddable;

@Embeddable
public class Amount {
    @JsonIgnore
    private static final String CURRENCY_ERROR_MESSAGE = "The currencies must be the same.";
    @JsonIgnore
    private static final String VALUE_ERROR_MESSAGE = "You must input a positive number to %s.";

    @Tag(1)
    private String currency;
    @Tag(2)
    private Double value;

    public Amount() {
    }

    public Amount(String currency, Double value) {
        this.currency = Optional.ofNullable(currency).map(String::toUpperCase).orElse(null);
        this.value = value;
    }

    public static Optional<Amount> createFromAmount(Amount amount) {
        return Optional.ofNullable(amount)
                .filter(a -> a.getCurrency() != null)
                .filter(a -> a.getValue() != null)
                .map(a -> new Amount(a.getCurrency(), a.getValue()));
    }

    public String getCurrency() {
        return currency != null ? currency.toUpperCase() : null;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double amount) {
        this.value = amount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Amount)) {
            return false;
        }

        Amount other = (Amount)obj;

        if (!Objects.equals(this.currency, other.currency)) {
            return false;
        }

        return Objects.equals(this.value, other.value);
    }

    public static Amount inSEK(Double value) {
        return new Amount("SEK", value);
    }

    public static Amount inNOK(Double value) {
        return new Amount("NOK", value);
    }

    public static Amount inDKK(Double value) {
        return new Amount("DKK", value);
    }

    public static Amount inEUR(Double value) {
        return new Amount("EUR", value);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("currency", currency)
                .add("value", value)
                .toString();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return Strings.isNullOrEmpty(currency) || value == null;
    }

    @JsonIgnore
    public boolean isPositive() {
        return value != null && value > 0;
    }

    @JsonIgnore
    public boolean isGreaterThan(int pivot) {
        return value > pivot;
    }

    @JsonIgnore
    public boolean isGreaterThan(double pivot) {
        return value > pivot;
    }

    @JsonIgnore
    public boolean isLessThan(int pivot) {
        return value < pivot;
    }

    @JsonIgnore
    public boolean isLessThan(double pivot) {
        return value < pivot;
    }

    @JsonIgnore
    public Amount add(double toAdd) {
        Preconditions.checkArgument(toAdd > 0, VALUE_ERROR_MESSAGE, "add");
        return new Amount(this.currency, this.value + toAdd);
    }

    @JsonIgnore
    public Amount subtract(double toSubtract) {
        Preconditions.checkArgument(toSubtract > 0, VALUE_ERROR_MESSAGE, "subtract");
        return new Amount(this.currency, this.value - toSubtract);
    }

    @JsonIgnore
    public Amount subtract(Amount toSubtract) {
        Preconditions.checkArgument(Objects.equals(this.currency, toSubtract.getCurrency()), CURRENCY_ERROR_MESSAGE);
        return subtract(toSubtract.getValue());
    }

    @JsonIgnore
    public Amount add(Amount toAdd) {
        Preconditions.checkArgument(Objects.equals(this.currency, toAdd.getCurrency()), CURRENCY_ERROR_MESSAGE);
        return add(toAdd.getValue());
    }
}
