package se.tink.backend.insights.core.valueobjects;

import java.util.Objects;

public class TransactionId {
    private String id;

    private TransactionId(String id) {
        this.id = id;
    }

    public String value() {
        return id;
    }

    public static TransactionId of(String id) {
        return new TransactionId(id);
    }

    @Override
    public String toString() {
        return id;
    }

    public boolean equals(TransactionId that) {
        return Objects.equals(id, that.value());
    }
}
