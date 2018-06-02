package se.tink.libraries.i18n;

/**
 * Used for having localized enum collections. This is sometimes needed when not having the catalog instance and you want
 * a static string instead of putting them as instances directly in the implementation. This also makes sharing the
 * translation strings a lot easier.
 * <p>
 * How to use:
 * Check example implementation below.
 * <p>
 * Catalog usage:
 * catalog.getString(ENUM_VALUE);
 */
public interface LocalizableEnum {
    LocalizableKey getKey();
}

/**
 * Example implementation:
 *
public enum EndUserMessage implements LocalizableEnum {
    BANKID_NO_RESPONSE(new LocalizableKey("No response from Mobile BankID. Have you opened the app?"));

    private final LocalizableKey key;

    EndUserMessage(LocalizableKey key) {
        Preconditions.checkNotNull(key);
        this.key = key;
    }

    @Override
    public LocalizableKey getKey() {
        return key;
    }
}
 */
