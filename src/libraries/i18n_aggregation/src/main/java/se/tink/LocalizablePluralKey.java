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
public class LocalizablePluralKey {
    private final String singular;
    private final String plural;

    public LocalizablePluralKey(String singular, String plural) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(singular));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(plural));
        this.singular = singular;
        this.plural = plural;
    }

    public String getSingular() {
        return singular;
    }

    public String getPlural() {
        return plural;
    }
}
