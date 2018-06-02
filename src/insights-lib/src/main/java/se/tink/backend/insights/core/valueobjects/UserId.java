package se.tink.backend.insights.core.valueobjects;

import java.util.Objects;

public class UserId {
    private String id;

    private UserId(String id) {
        this.id = id;
    }

    public String value() {
        return id;
    }

    public static UserId of(String id) {
        return new UserId(id);
    }

    @Override
    public String toString() {
        return id;
    }

    public boolean equals(UserId that) {
        return Objects.equals(id, that.value());
    }

}
