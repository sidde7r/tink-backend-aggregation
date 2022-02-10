package se.tink.libraries.i18n_aggregation;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Used for localizing static strings. This is sometimes needed when not having the catalog instance
 * and you want a static string instead of putting them as instances directly in the implementation.
 * This also makes sharing the translation strings a lot easier.
 *
 * <p>How to use: private static final String MY_TRANSLATED = new LocalizableKey(…)
 *
 * <p>… catalog.getString(MY_TRANSLATED); (additionally with parameters if parametrized)
 */
public class LocalizableParametrizedKey {
    private final String key;
    private Object[] parameters;

    public LocalizableParametrizedKey(String key) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key));
        this.key = key;
    }

    private LocalizableParametrizedKey(String key, Object... parameters) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key));
        this.key = key;
        this.parameters = parameters;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public String get() {
        return key;
    }

    public LocalizableParametrizedKey cloneWith(Object... parameters) {
        return new LocalizableParametrizedKey(key, parameters);
    }
}
