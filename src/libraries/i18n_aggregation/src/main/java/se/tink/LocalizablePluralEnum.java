package se.tink.libraries.i18n_aggregation;

/**
 * Used for having localized enum collections. This is sometimes needed when not having the catalog
 * instance and you want a static string instead of putting them as instances directly in the
 * implementation. This also makes sharing the translation strings a lot easier.
 *
 * <p>How to use: Check example implementation below.
 *
 * <p>Catalog usage: catalog.getString(ENUM_VALUE, n);
 */
public interface LocalizablePluralEnum {
    LocalizablePluralKey getKey();
}

/**
 * Example implementation:
 *
 * <p>public enum EndUserMessage implements LocalizableEnum { BANKID_NO_RESPONSE(new
 * LocalizableKey("No response from Mobile BankID. Have you opened the app?"));
 *
 * <p>private final LocalizableKey key;
 *
 * <p>EndUserMessage(LocalizableKey key) { Preconditions.checkNotNull(key); this.key = key;
 * } @Override public LocalizableKey getKey() { return key; } }
 */
