package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.libraries.i18n.LocalizableKey;

@Getter
@RequiredArgsConstructor
public enum NemId2FAMethod {
    CODE_APP(new LocalizableKey("Code app"), "codeApp", 1),
    CODE_CARD(new LocalizableKey("Code card"), "codeCard", 2),
    CODE_TOKEN(new LocalizableKey("Code token"), "codeToken", 3);

    private final LocalizableKey userFriendlyName;
    private final String supplementalInfoKey;
    private final Integer supplementalInfoOrder;

    public static Optional<NemId2FAMethod> getMethodBySupplementalInfoKey(String key) {
        return Stream.of(NemId2FAMethod.values())
                .filter(method -> method.getSupplementalInfoKey().equals(key))
                .findFirst();
    }
}
