package se.tink.libraries.amount;

import com.google.common.base.Preconditions;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class ExactCurrencyAmount implements Comparable<ExactCurrencyAmount> {
    private final BigDecimal value;
    private final String currencyCode;

    public ExactCurrencyAmount(BigDecimal value, String currencyCode) {
        Preconditions.checkNotNull(value, "Value must not be null.");
        this.value = value;
        this.currencyCode = currencyCode;
    }

    public static ExactCurrencyAmount of(BigDecimal i, String currencyCode) {
        return new ExactCurrencyAmount(i, currencyCode);
    }

    public static ExactCurrencyAmount of(String i, String currencyCode) {
        return new ExactCurrencyAmount(new BigDecimal(i), currencyCode);
    }

    public static ExactCurrencyAmount of(double i, String currencyCode) {
        return new ExactCurrencyAmount(BigDecimal.valueOf(i), currencyCode);
    }

    public static ExactCurrencyAmount zero(String currencyCode) {
        return of(BigDecimal.ZERO, currencyCode);
    }

    public double getDoubleValue() {
        return value.doubleValue();
    }

    public String getStringValue(Locale locale) {
        NumberFormat currencyInstance = NumberFormat.getCurrencyInstance(locale);
        // You cannot suggest to improve this code by replacing the currency symbol e.g. "$"
        // It doesn't work because currency symbol is dependent on your system locale.
        // E.g. 5 US dollars, can yield both "$5" or "USD 5", depending on your system locale.
        // And you don't want to change your system locale
        return getStringValue(currencyInstance).replaceAll("[^0123456789.,()-]", "").trim();
    }

    public String getStringValue(NumberFormat currencyInstance) {
        return currencyInstance.format(value);
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

    public ExactCurrencyAmount negate() {
        return new ExactCurrencyAmount(this.getExactValue().negate(), this.getCurrencyCode());
    }

    public ExactCurrencyAmount subtract(ExactCurrencyAmount amount) {
        return new ExactCurrencyAmount(
                this.getExactValue().subtract(amount.getExactValue()), this.getCurrencyCode());
    }

    public ExactCurrencyAmount add(ExactCurrencyAmount amount) {
        return new ExactCurrencyAmount(
                this.getExactValue().add(amount.getExactValue()), this.getCurrencyCode());
    }

    public static ExactCurrencyAmount inSEK(double value) {
        return ExactCurrencyAmount.of(value, "SEK");
    }

    public static ExactCurrencyAmount inNOK(double value) {
        return ExactCurrencyAmount.of(value, "NOK");
    }

    public static ExactCurrencyAmount inDKK(double value) {
        return ExactCurrencyAmount.of(value, "DKK");
    }

    public static ExactCurrencyAmount inEUR(double value) {
        return ExactCurrencyAmount.of(value, "EUR");
    }

    @Override
    public int compareTo(ExactCurrencyAmount other) {
        return value.compareTo(other.value);
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
