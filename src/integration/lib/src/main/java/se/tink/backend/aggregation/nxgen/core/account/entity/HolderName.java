package se.tink.backend.aggregation.nxgen.core.account.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Objects;

import java.util.Optional;

public class HolderName {

    private final String value;

    @JsonCreator
    public HolderName(String value) {
        this.value = value;
    }

    public static String toString(HolderName holderName) {
        return Optional.ofNullable(holderName).map(holder -> holder.value).orElse(null);
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HolderName that = (HolderName) o;
        return Objects.equal(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.value);
    }
}
