package se.tink.backend.aggregation.nxgen.core.account.entity;

import com.google.common.base.Preconditions;

public class Party {

    private String name;
    private Role role;

    public Party(String name, Role role) {
        Preconditions.checkNotNull(name, "name must not be null.");
        Preconditions.checkNotNull(role, "role must not be null.");
        this.name = name;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public enum Role {
        HOLDER,
        AUTHORIZED_USER,
        OTHER,
        UNKNOWN;
    }
}
