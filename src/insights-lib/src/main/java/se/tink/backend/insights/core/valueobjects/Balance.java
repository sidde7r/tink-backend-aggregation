package se.tink.backend.insights.core.valueobjects;

import java.util.Objects;

public class Balance {
    private double amount;

    private Balance(double amount) {
        this.amount = amount;
    }

    public double value() {
        return amount;
    }

    public static Balance of(double amount) {
        return new Balance(amount);
    }

    public boolean equals(Balance that) {
        return Objects.equals(amount, that.value());
    }

    public boolean greaterThan(double value) {
        return this.amount > value;
    }
}
