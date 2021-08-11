package se.tink.backend.aggregation.nxgen.core.account.entity;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang.WordUtils;

@ToString
@EqualsAndHashCode
public class Party {

    private final String name;
    private final Role role;

    public Party(String name, Role role) {
        char[] delimiters = " -'".toCharArray();
        this.name = WordUtils.capitalizeFully(name, delimiters);
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
