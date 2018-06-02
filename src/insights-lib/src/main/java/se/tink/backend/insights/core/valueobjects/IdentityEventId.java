package se.tink.backend.insights.core.valueobjects;

import java.util.Objects;

public class IdentityEventId {
    private String id;

    private IdentityEventId(String id) {
        this.id = id;
    }

    public String value() {
        return id;
    }

    public static IdentityEventId of(String id) {
        return new IdentityEventId(id);
    }

    @Override
    public String toString() {
        return id;
    }

    public boolean equals(IdentityEventId that) {
        return Objects.equals(id, that.value());
    }
}
