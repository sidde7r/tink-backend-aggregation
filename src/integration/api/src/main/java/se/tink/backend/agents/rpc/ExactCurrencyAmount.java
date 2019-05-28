package se.tink.backend.agents.rpc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class ExactCurrencyAmount implements Comparable<ExactCurrencyAmount> {
    private BigDecimal value;
    private String currencyCode;

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

    public static ExactCurrencyAmount of(Number i, String currencyCode) {
        return new ExactCurrencyAmount(new BigDecimal(i.toString()), currencyCode);
    }

    public static ExactCurrencyAmount of(ExactCurrencyAmount exactCurrencyAmount) {
        return new ExactCurrencyAmount(
                exactCurrencyAmount.getExactValue(), exactCurrencyAmount.getCurrencyCode());
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
                this.getCurrencyCode() == null
                        ? other.getCurrencyCode() == null
                        : this.getCurrencyCode().equals(other.getCurrencyCode());
        boolean valueEquality = this.value.compareTo(other.value) == 0;
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
