package se.tink.backend.insights.core.valueobjects;

import java.util.Objects;

public class AccountId {
    private String id;

    private AccountId(String id) {
        this.id = id;
    }

    public String value() {
        return id;
    }

    public static AccountId of(String id) {
        return new AccountId(id);
    }

    @Override
    public String toString() {
        return id;
    }

    public boolean equals(AccountId that) {
        return Objects.equals(id, that.value());
    }
}
