package se.tink.backend.aggregation.provider.configuration.controllers;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration;
import se.tink.backend.core.Field;
import se.tink.libraries.i18n.Catalog;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class Localizator {

    private static Field translate(Catalog catalog, Field field) {
        if (!Strings.isNullOrEmpty(field.getHelpText())) {
            field.setHelpText(catalog.getString(field.getHelpText()));
        }
        if (!Strings.isNullOrEmpty(field.getDescription())) {
            field.setDescription(catalog.getString(field.getDescription()));
        }
        if (!Strings.isNullOrEmpty(field.getHint())) {
            field.setHint(catalog.getString(field.getHint()));
        }
        if (!Strings.isNullOrEmpty(field.getPatternError())) {
            field.setPatternError(catalog.getString(field.getPatternError()));
        }

        if (field.getChildren() != null) {
            field.setChildren(field.getChildren().stream()
                    .map(f -> translate(catalog, f))
                    .collect(Collectors.toList()));
        }

        return field;
    }

    public static List<ProviderConfiguration> translate(Locale locale, List<ProviderConfiguration> providerConfigurations) {
        return providerConfigurations.stream()
                .map(providerConfiguration -> translate(locale, providerConfiguration))
                .collect(Collectors.toList());
    }

    public static ProviderConfiguration translate(Locale locale, ProviderConfiguration providerConfiguration) {
        Catalog catalog = Catalog.getCatalog(locale);
        if (!Strings.isNullOrEmpty(providerConfiguration.getDisplayDescription())) {
            providerConfiguration.setDisplayDescription(catalog.getString(providerConfiguration.getDisplayDescription()));
        }
        if (!Strings.isNullOrEmpty(providerConfiguration.getPasswordHelpText())) {
            providerConfiguration.setPasswordHelpText(catalog.getString(providerConfiguration.getPasswordHelpText()));
        }

        List<Field> fields = providerConfiguration.getFields();

        if (fields != null) {
            providerConfiguration.setFields(fields.stream()
                    .map(field -> translate(catalog, field))
                    .collect(Collectors.toList()));
        }

        return providerConfiguration;
    }

}
