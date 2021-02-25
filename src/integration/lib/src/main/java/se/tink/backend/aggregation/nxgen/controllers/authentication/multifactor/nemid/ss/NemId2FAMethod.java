package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.libraries.i18n.LocalizableKey;

@Getter
@RequiredArgsConstructor
public enum NemId2FAMethod {
    CODE_APP(
            new LocalizableKey("Code app"),
            "codeApp",
            "https://cdn.tink.se/provider-images/dk/nemid/nemid-code-app.png",
            1),
    CODE_CARD(
            new LocalizableKey("Code card"),
            "codeCard",
            "https://cdn.tink.se/provider-images/dk/nemid/nemid-code-card.png",
            2),
    CODE_TOKEN(
            new LocalizableKey("Code token"),
            "codeToken",
            "https://cdn.tink.se/provider-images/dk/nemid/nemid-code-token.png",
            3);

    private final LocalizableKey userFriendlyName;
    private final String supplementalInfoKey;
    private final String supplementalInfoIconUrl;
    private final Integer supplementalInfoOrder;

    public static Optional<NemId2FAMethod> getMethodBySupplementalInfoKey(String key) {
        return Stream.of(NemId2FAMethod.values())
                .filter(method -> method.getSupplementalInfoKey().equals(key))
                .findFirst();
    }
}
