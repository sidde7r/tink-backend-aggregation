package se.tink.libraries.i18n_aggregation;

/**
 * Used for having localized enum collections. This is sometimes needed when not having the catalog
 * instance and you want a static string instead of putting them as instances directly in the
 * implementation. This also makes sharing the translation strings a lot easier.
 *
 * <p>How to use: Check example implementation below.
 *
 * <p>Catalog usage: catalog.getString( ENUM_VALUE.with( parameters... ) ); or
 * LocalizableParametrizedKey message = ENUM_VALUE.with( parameters... ); catalog.getString(
 * ENUM_VALUE );
 */
public interface LocalizableParametrizedEnum {
    LocalizableParametrizedKey getKey();

    LocalizableParametrizedKey cloneWith(Object... parameters);
}

/**
 * Example implementation of parametrized enum
 *
 * <p>Since enums are static, and the parameters are dynamic, the enum itself cannot specify the
 * parameters. The parameters cannot be injected to the enum either, since that would change all
 * instances of that enum to have the same parameters between threads.
 *
 * <p>There for the LocalizableParametrized key stored in the enum do not have any parameters
 * specified, instead the with() method will build a new instance based on the injected parameters.
 *
 * <p>public enum EndUserMessageParametrized implements LocalizableParametrizedEnum {
 * CURRENCY_NOT_AVAILABLE(new LocalizableParametrizedKey("Currency {0} not available"));
 *
 * <p>private LocalizableParametrizedKey key;
 *
 * <p>EndUserMessageParametrized(LocalizableParametrizedKey key) { this.key = key; } @Override
 * public LocalizableParametrizedKey getKey() { return key; } @Override public
 * LocalizableParametrizedKey cloneWith(Object... parameters) { return key.cloneWith(parameters); }
 * }
 */
