package se.tink.libraries.i18n;

import com.google.common.collect.Maps;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

public class Catalog {
    private static final Map<Locale, Catalog> catalogs = Maps.newHashMap();
    private static final Object catalogsLock = new Object();
    private static final Logger log = LoggerFactory.getLogger(Catalog.class);

    private static final String I18N_MESSAGES_BUNDLE = "se.tink.libraries.i18n.Messages";
    private static final Locale BASE_LOCALE = getLocale("en_US");

    public static String format(String message, Object... params) {
        return MessageFormat.format(message.replace("'", "''"), params);
    }

    public static Catalog getCatalog(String locale) {
        return getCatalog(getLocale(locale));
    }

    public static Catalog getCatalog(Locale locale) {
        synchronized (catalogsLock) {
            if (!catalogs.containsKey(locale)) {
                catalogs.put(locale, new Catalog(locale));
            }
        }

        return catalogs.get(locale);
    }

    protected I18n i18n;

    private Catalog() {
        // NOP
    }

    public Catalog(Locale locale) {
        if (!locale.equals(BASE_LOCALE)) {
            try {
                i18n =
                        I18nFactory.getI18n(
                                Catalog.class, I18N_MESSAGES_BUNDLE, locale, I18nFactory.FALLBACK);
            } catch (Exception e) {
                log.error("Could not instantiate catalog for locale: {}", locale);
            }
        }
    }

    public static Locale getLocale(String locale) {
        if (locale.length() == 2) {
            return new Locale(locale);
        } else if (locale.length() >= 5) {
            return new Locale(locale.substring(0, 2), locale.substring(3, 5));
        } else {
            return null;
        }
    }

    public String getPluralString(String singularMessage, String pluralMessage, long n) {
        if (i18n != null) {
            return i18n.trn(singularMessage, pluralMessage, n);
        } else {
            return (n == 1 ? singularMessage : pluralMessage);
        }
    }

    public String getString(String message) {
        if (i18n != null) {
            return i18n.tr(message);
        } else {
            return message;
        }
    }

    public String getString(LocalizableKey localizableKey) {
        return getString(localizableKey.get());
    }

    public String getString(LocalizableParametrizedKey localizableParametrizedKey) {
        return Catalog.format(
                getString(localizableParametrizedKey.get()),
                localizableParametrizedKey.getParameters());
    }

    public String getString(
            LocalizableParametrizedKey localizableParametrizedKey, Object... parameters) {
        return Catalog.format(getString(localizableParametrizedKey.get()), parameters);
    }

    public String getPluralString(LocalizablePluralKey localizablePluralKey, long n) {
        return getPluralString(
                localizablePluralKey.getSingular(), localizablePluralKey.getPlural(), n);
    }

    public String getString(LocalizableEnum localizableEnum) {
        return getString(localizableEnum.getKey().get());
    }

    public I18n getI18n() {
        return i18n;
    }
}
