package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.entities;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MembershipType {
    UNKNOWN("", ""),
    PART("1", "part"),
    PRO("2", "pro");

    private final String value;

    @Getter private final String name;

    public static MembershipType fromString(String text) {
        return Arrays.stream(MembershipType.values())
                .filter(type -> type.value.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
