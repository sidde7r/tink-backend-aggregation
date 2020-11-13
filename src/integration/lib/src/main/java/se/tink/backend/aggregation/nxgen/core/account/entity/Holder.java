package se.tink.backend.aggregation.nxgen.core.account.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import se.tink.backend.agents.rpc.HolderIdentity;
import se.tink.backend.agents.rpc.HolderRole;

public class Holder {
    private String name;
    private Role role;

    private Holder(String name, Role role) {
        this.name = name;
        this.role = role;
    }

    public static Holder of(@Nonnull String name, Role role) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "name can't be null or empty");
        return new Holder(name, role);
    }

    public static Holder of(@Nonnull String name) {
        return of(name, null);
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Holder holder = (Holder) o;
        return Objects.equals(name, holder.name) && role == holder.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, role);
    }

    @Override
    public String toString() {
        return "Holder{" + "name='" + name + '\'' + ", role=" + role + '}';
    }

    public HolderIdentity toSystemHolder() {
        HolderIdentity systemHolder = new HolderIdentity();
        systemHolder.setName(name);
        systemHolder.setRole(Optional.ofNullable(role).map(Role::toSystemRole).orElse(null));
        return systemHolder;
    }

    public enum Role {
        HOLDER(HolderRole.HOLDER),
        AUTHORIZED_USER(HolderRole.AUTHORIZED_USER),
        OTHER(HolderRole.OTHER);

        private final HolderRole systemRole;

        Role(HolderRole systemRole) {
            this.systemRole = systemRole;
        }

        public HolderRole toSystemRole() {
            return systemRole;
        }
    }
}
