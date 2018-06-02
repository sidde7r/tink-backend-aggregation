package se.tink.backend.insights.core.valueobjects;

import java.util.Objects;

public class InsightActionId {
    private String id;

    private InsightActionId(String id) {
        this.id = id;
    }

    public String value() {
        return id;
    }

    public static InsightActionId of(String id) {
        return new InsightActionId(id);
    }

    @Override
    public String toString() {
        return id;
    }

    public boolean equals(InsightActionId that) {
        return Objects.equals(id, that.value());
    }
}
