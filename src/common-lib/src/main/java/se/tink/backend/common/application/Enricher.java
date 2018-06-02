package se.tink.backend.common.application;

import com.google.common.collect.ImmutableMap;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

public abstract class Enricher {
    protected String getString(Catalog catalog, ImmutableMap<String, LocalizableKey> map, String key) {
        return getString(catalog, map, key, null);
    }
    
    protected String getString(Catalog catalog, ImmutableMap<String, LocalizableKey> map, String key, String fallback) {
        if (!map.containsKey(key)) {
            return fallback;
        }

        return catalog.getString(map.get(key));
    }
    
    /*
     * Uses the key as fallback if no localizable key mapping exists for the supplied key.
     */
    protected String tryGetString(Catalog catalog, ImmutableMap<String, LocalizableKey> map, String key) {
        return getString(catalog, map, key, key);
    }
}
