package se.tink.backend.aggregation.nxgen.core.account.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang.WordUtils;

@Getter
@ToString
@EqualsAndHashCode
public class Party {

    private final String name;
    private final Role role;
    private final List<Address> addresses;

    public Party(String name, Role role) {
        char[] delimiters = " -'".toCharArray();
        this.name = WordUtils.capitalizeFully(name, delimiters);
        this.role = role;
        this.addresses = new ArrayList<>();
    }

    public Party withAddress(Address address) {
        addresses.add(address);
        return this;
    }

    public Party withAddresses(Address... addresses) {
        this.addresses.addAll(Arrays.asList(addresses));
        return this;
    }

    public enum Role {
        HOLDER,
        AUTHORIZED_USER,
        OTHER,
        UNKNOWN
    }
}
