package se.tink.libraries.amount;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.Objects;

@Deprecated
public class Amount extends Number {

    private static final int SCALE = 2;

    private String currency;
    private double value;

    public Amount() {
        this.currency = null;
        this.value = 0D;
    }

    public Amount(String currency, Number value) {
        this(currency, value != null ? value.doubleValue() : Double.NaN);
    }

    public Amount(String currency, double value) {
        this.currency = currency != null ? currency.toUpperCase() : null;
        this.value = value;
    }

    public static Amount inSEK(Number value) {
        return new Amount("SEK", value);
    }

    public String getCurrency() {
        return currency;
    }

    /**
     * Should preferably return double rather than Double, but too much code expects a reference
     * type here. It's probably better to call either {@link #doubleValue()} or {@link
     * #toBigDecimal()}}.
     */
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

    @JsonIgnore
    public boolean isEmpty() {
        return Strings.isNullOrEmpty(currency) || !Double.isFinite(value);
    }

    @JsonIgnore
    public boolean isLessThan(double pivot) {
        return value < pivot;
    }

    public BigDecimal toBigDecimal() {
        if (Double.isFinite(value)) {
            return BigDecimal.valueOf(Math.round(value * 100D), SCALE);
        } else {
            return null; // Not nice, but it's what's expected...
        }
    }

    @Override
    public int intValue() {
        return (int) longValue();
    }

    @Override
    public long longValue() {
        return Math.round(value);
    }

    @Override
    public float floatValue() {
        return (float) value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    public Amount negate() {
        return new Amount(currency, -value);
    }
}
