package se.tink.backend.rpc;

import se.tink.libraries.i18n.Catalog;

public class UpdateI18nSettingsCommand {
    private String localeCode;

    public UpdateI18nSettingsCommand(String localeCode) {
        validate(localeCode);
        this.localeCode = localeCode;
    }

    private void validate(String localeCode) {
        try {
            Catalog.getLocale(localeCode);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid locale code: " + localeCode);
        }
    }

    public String getLocaleCode() {
        return localeCode;
    }
}
