package se.tink.libraries.i18n_aggregation;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;

/**
 * Used for localizing static strings. This is sometimes needed when not having the catalog instance
 * and you want a static string instead of putting them as instances directly in the implementation.
 * This also makes sharing the translation strings a lot easier.
 *
 * <p>How to use: private static final String MY_TRANSLATED = new LocalizableKey(…)
 *
 * <p>… catalog.getString(MY_TRANSLATED); (additionally with parameters if parametrized)
 */
@EqualsAndHashCode
public class LocalizableKey {
    private final String key;

    public LocalizableKey(String key) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key));
        this.key = key;
    }

    public String get() {
        return key;
    }

    public static LocalizableKey of(String key) {
        return new LocalizableKey(key);
    }
}
