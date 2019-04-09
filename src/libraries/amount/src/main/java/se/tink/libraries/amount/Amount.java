package se.tink.libraries.amount;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

public class Amount extends Number {
    @JsonIgnore
    private static final String CURRENCY_ERROR_MESSAGE = "The currencies must be the same.";

    private static final int SCALE = 2;

    private String currency;
    private double value;

    public static Amount valueOf(String currency, long unscaledValue, int scale) {
        Objects.requireNonNull(currency);
        return new Amount(currency, unscaledValue, scale);
    }

    private static BigDecimal toBigDecimal(long unscaledValue, int scale) {
        BigDecimal value = BigDecimal.valueOf(unscaledValue, scale);
        if (scale != SCALE) {
            value = value.setScale(SCALE, RoundingMode.HALF_EVEN);
        }
        return value;
    }

    public Amount() {
        this.currency = null;
        this.value = 0D;
    }

    public Amount(String currency, long unscaledValue, int scale) {
        this(currency, toBigDecimal(unscaledValue, scale));
    }

    public Amount(String currency, Number value) {
        this(currency, value != null ? value.doubleValue() : Double.NaN);
    }

    public Amount(String currency, double value) {
        this.currency = currency != null ? currency.toUpperCase() : null;
        this.value = value;
    }

    public static Optional<Amount> createFromAmount(Amount amount) {
        return Optional.ofNullable(amount)
                .filter(a -> a.getCurrency() != null)
                .filter(a -> Double.isFinite(a.getValue()))
                .map(a -> new Amount(a.getCurrency(), a.getValue()));
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency != null ? currency.toUpperCase() : null;
    }

    /**
     * Should preferably return double rather than Double, but too much code expects a reference
     * type here. It's probably better to call either {@link #doubleValue()} or {@link
     * #toBigDecimal()}}.
     */
    public Double getValue() {
        return value;
    }

    public void setValue(Number amount) {
        if (amount != null) {
            setValue(amount.doubleValue());
        } else {
            setValue(Double.NaN);
        }
    }

    public void setValue(long unscaledValue, int scale) {
        setValue(toBigDecimal(unscaledValue, scale));
    }

    public void setValue(double amount) {
        this.value = amount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Amount)) {
            return false;
        }

        Amount other = (Amount) obj;

        if (!Objects.equals(this.currency, other.currency)) {
            return false;
        }

        return Objects.equals(this.value, other.value);
    }

    public static Amount inSEK(Number value) {
        return new Amount("SEK", value);
    }

    public static Amount inNOK(Number value) {
        return new Amount("NOK", value);
    }

    public static Amount inDKK(Number value) {
        return new Amount("DKK", value);
    }

    public static Amount inEUR(Number value) {
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
        return Strings.isNullOrEmpty(currency) || !Double.isFinite(value);
    }

    @JsonIgnore
    public boolean isZero() {
        return value == 0;
    }

    @JsonIgnore
    public boolean isPositive() {
        return Double.isFinite(value) && value > 0;
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
        return new Amount(this.currency, this.value + toAdd);
    }

    @JsonIgnore
    public Amount subtract(double toSubtract) {

        return new Amount(this.currency, this.value - toSubtract);
    }

    @JsonIgnore
    public Amount subtract(Amount toSubtract) {
        Preconditions.checkArgument(
                Objects.equals(this.currency, toSubtract.getCurrency()), CURRENCY_ERROR_MESSAGE);
        return subtract(toSubtract.getValue());
    }

    @JsonIgnore
    public Amount add(Amount toAdd) {
        Preconditions.checkArgument(
                Objects.equals(this.currency, toAdd.getCurrency()), CURRENCY_ERROR_MESSAGE);
        return add(toAdd.getValue());
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

    /**
     * Removes negative sign.
     *
     * @return Unsigned amount
     */
    public Amount stripSign() {
        return new Amount(currency, Math.abs(value));
    }
}
