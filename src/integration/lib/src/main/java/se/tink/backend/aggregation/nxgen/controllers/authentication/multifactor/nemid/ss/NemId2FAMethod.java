package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
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

    public static Optional<NemId2FAMethod> getMethodBySupplementalInfoKey(
            String key, List<NemId2FAMethod> availableMethods) {
        if (shouldHandleBackwardCompatibility(key)) {
            int index = Integer.parseInt(key) - 1;
            return Optional.of(availableMethods.get(index));
        }
        return availableMethods.stream()
                .filter(method -> method.getSupplementalInfoKey().equals(key))
                .findFirst();
    }

    private static boolean shouldHandleBackwardCompatibility(String key) {
        return NumberUtils.isDigits(key);
    }
}
