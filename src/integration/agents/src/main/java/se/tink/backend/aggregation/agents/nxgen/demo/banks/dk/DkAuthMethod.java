package se.tink.backend.aggregation.agents.nxgen.demo.banks.dk;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Getter
@RequiredArgsConstructor
public enum DkAuthMethod {
    NEM_ID("nemId", "1"),
    MIT_ID("mitId", "2");

    private final List<String> supplementalInfoKeys;

    DkAuthMethod(String... supplementalInfoKeys) {
        this.supplementalInfoKeys = Arrays.asList(supplementalInfoKeys);
    }

    public static DkAuthMethod getBySupplementalInfoKey(String supplementalInfoKey) {
        for (DkAuthMethod method : DkAuthMethod.values()) {
            boolean hasMatchingKey =
                    method.getSupplementalInfoKeys().stream()
                            .anyMatch(
                                    methodKey ->
                                            StringUtils.equalsIgnoreCase(
                                                    methodKey, supplementalInfoKey));
            if (hasMatchingKey) {
                return method;
            }
        }
        throw new IllegalStateException("Unknown method key: " + supplementalInfoKey);
    }
}
