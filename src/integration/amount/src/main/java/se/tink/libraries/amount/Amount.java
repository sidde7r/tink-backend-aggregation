package se.tink.libraries.amount;

import com.google.common.base.MoreObjects;
import java.math.BigDecimal;
import java.util.Objects;

@Deprecated
public class Amount {

    private static final int SCALE = 2;

    private String currency;
    private double value;

    public Amount(String currency, Number value) {
        this(currency, value != null ? value.doubleValue() : Double.NaN);
    }

    public Amount(String currency, double value) {
        this.currency = currency != null ? currency.toUpperCase() : null;
        this.value = value;
    }

    public String getCurrency() {
        return currency;
    }

    public Double getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Amount)) {
            return false;
        }

        Amount other = (Amount) obj;

        if (!Objects.equals(this.currency, other.currency)) {
            return false;
        }

        return Objects.equals(this.value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, value);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("currency", currency)
                .add("value", value)
                .toString();
    }

    public BigDecimal toBigDecimal() {
        if (Double.isFinite(value)) {
            return BigDecimal.valueOf(Math.round(value * 100D), SCALE);
        } else {
            return null; // Not nice, but it's what's expected...
        }
    }

    public Amount negate() {
        return new Amount(currency, -value);
    }
}
