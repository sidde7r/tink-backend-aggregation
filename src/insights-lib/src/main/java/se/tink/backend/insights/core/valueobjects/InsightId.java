package se.tink.backend.insights.core.valueobjects;

import java.util.Objects;

public class InsightId {
    private String id;

    private InsightId(String id) {
        this.id = id;
    }

    public String value() {
        return id;
    }

    public static InsightId of(String id) {
        return new InsightId(id);
    }

    @Override
    public String toString() {
        return id;
    }

    public boolean equals(InsightId that) {
        return Objects.equals(id, that.value());
    }
}
