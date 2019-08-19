package se.tink.libraries.amount;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class ExactCurrencyAmount implements Comparable<ExactCurrencyAmount> {
    private final BigDecimal value;
    private final String currencyCode;

    public ExactCurrencyAmount(BigDecimal value, String currencyCode) {
        this.value = value;
        this.currencyCode = currencyCode;
    }

    private ExactCurrencyAmount(double value, String currencyCode) {
        this(BigDecimal.valueOf(value), currencyCode);
    }

    private ExactCurrencyAmount(long value, int scale, String currencyCode) {
        this(new BigDecimal(BigInteger.valueOf(value), scale), currencyCode);
    }

    public static ExactCurrencyAmount of(BigDecimal i, String currencyCode) {
        return new ExactCurrencyAmount(i, currencyCode);
    }

    public static ExactCurrencyAmount of(String i, String currencyCode) {
        return new ExactCurrencyAmount(new BigDecimal(i), currencyCode);
    }

    public static ExactCurrencyAmount of(ExactCurrencyAmount amount) {
        return new ExactCurrencyAmount(amount.getExactValue(), amount.getCurrencyCode());
    }

    public double getDoubleValue() {
        return value.doubleValue();
    }

    public long getUnscaledValue() {
        return value.unscaledValue().longValue();
    }

    public int getScale() {
        return value.scale();
    }

    public BigDecimal getExactValue() {
        return value;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExactCurrencyAmount other = (ExactCurrencyAmount) o;
        boolean currencyCodeEquality =
                Objects.equals(this.getCurrencyCode(), other.getCurrencyCode());
        boolean valueEquality = (this.value.compareTo(other.value) == 0);
        return valueEquality && currencyCodeEquality;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, currencyCode);
    }

    public ExactCurrencyAmount abs() {
        return new ExactCurrencyAmount(this.getExactValue().abs(), this.getCurrencyCode());
    }

    @Override
    public int compareTo(ExactCurrencyAmount other) {
        return compareTo(other.value);
    }

    public int compareTo(BigDecimal other) {
        return this.value.compareTo(other);
    }

    @Override
    public String toString() {
        return "ExactCurrencyAmount{"
                + "value="
                + value
                + ", currencyCode='"
                + currencyCode
                + '\''
                + '}';
    }
}
