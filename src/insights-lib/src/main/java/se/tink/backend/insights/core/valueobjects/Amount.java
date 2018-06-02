package se.tink.backend.insights.core.valueobjects;

import java.util.Objects;

public class Amount {
    private double amount;

    private Amount(double amount) {
        this.amount = amount;
    }

    public double value() {
        return amount;
    }

    public static Amount of(double amount) {
        return new Amount(amount);
    }

    public boolean equals(Amount that) {
        return Objects.equals(amount, that.value());
    }

    public boolean greaterThan(double value) {
        return this.amount > value;
    }

    public int getFlooredValue() {
        return (int) Math.floor(amount);
    }
}
