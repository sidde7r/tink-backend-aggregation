package se.tink.backend.core;

public class I18nSettings {
    private String localeCode;

    public I18nSettings(String localeCode) {
        this.localeCode = localeCode;
    }

    public String getLocaleCode() {
        return localeCode;
    }
}
