package se.tink.backend.aggregation.agents.nxgen.demo.banks.dk;

import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DkAuthMethod {
    NEM_ID("nemId"),
    MIT_ID("mitId");

    private final String supplementalInfoKey;

    public static DkAuthMethod getBySupplementalInfoKey(String key) {
        return Stream.of(DkAuthMethod.values())
                .filter(method -> method.getSupplementalInfoKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unknown method key: " + key));
    }
}
