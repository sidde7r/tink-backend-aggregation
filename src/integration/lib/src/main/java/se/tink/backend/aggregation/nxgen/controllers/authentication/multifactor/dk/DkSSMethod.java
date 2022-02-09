package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DkSSMethod {
    MIT_ID("mitId"),
    NEM_ID("nemId");

    private final String supplementalInfoKey;

    public static Optional<DkSSMethod> getBySupplementalInfoKey(String supplementalInfoKey) {
        return Stream.of(DkSSMethod.values())
                .filter(method -> method.getSupplementalInfoKey().equals(supplementalInfoKey))
                .findFirst();
    }
}
