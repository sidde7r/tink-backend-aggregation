package se.tink.backend.agents.rpc;

import com.google.common.base.MoreObjects;
import java.util.Objects;

public class HolderIdentity {
    private String name;
    private HolderRole role;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HolderRole getRole() {
        return role;
    }

    public void setRole(HolderRole role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HolderIdentity that = (HolderIdentity) o;
        return Objects.equals(name, that.name) && role == that.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, role);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name == null ? null : "***")
                .add("role", role)
                .toString();
    }
}
