package src.integration.nemid;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.xnap.commons.i18n.I18n;
import se.tink.libraries.i18n.Catalog;

@Getter
@RequiredArgsConstructor
public enum NemIdSupportedLanguageCode {
    EN("en"),
    DA("da");

    public static final NemIdSupportedLanguageCode DEFAULT_LANGUAGE_CODE =
            NemIdSupportedLanguageCode.EN;

    private final String isoLanguageCode;

    // e.g. "en" language code should match "en", "en-GB", "en-US", ...
    public boolean doesMatchLanguageCode(String languageCode) {
        return languageCode.toLowerCase().startsWith(isoLanguageCode.toLowerCase());
    }

    public static NemIdSupportedLanguageCode getFromCatalogOrDefault(Catalog catalog) {
        String userLanguageCode =
                Optional.ofNullable(catalog.getI18n())
                        .map(I18n::getLocale)
                        .map(Locale::getLanguage)
                        .orElse("");

        return Stream.of(NemIdSupportedLanguageCode.values())
                .filter(languageCode -> languageCode.doesMatchLanguageCode(userLanguageCode))
                .findFirst()
                .orElse(DEFAULT_LANGUAGE_CODE);
    }
}
