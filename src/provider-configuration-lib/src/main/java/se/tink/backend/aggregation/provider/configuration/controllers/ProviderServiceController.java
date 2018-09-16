package se.tink.backend.aggregation.provider.configuration.controllers;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfigurationDAO;
import se.tink.backend.core.Field;
import se.tink.libraries.i18n.Catalog;

public class ProviderServiceController {
    private static final Logger log = LoggerFactory.getLogger(ProviderServiceController.class);

    private final ProviderConfigurationDAO providerConfigurationDAO;

    @Inject
    public ProviderServiceController(ProviderConfigurationDAO providerConfigurationDAO) {
        this.providerConfigurationDAO = providerConfigurationDAO;
    }

    public List<ProviderConfiguration> list(Locale locale) {
        return translate(locale, providerConfigurationDAO.findAll());
    }

    public List<ProviderConfiguration> list(Locale locale, ClusterId clusterId) {
        return translate(locale, providerConfigurationDAO.findAllByClusterId(clusterId.getId()));
    }

    public List<ProviderConfiguration> listByMarket(Locale locale, ClusterId clusterId, String market) {
        return translate(locale, providerConfigurationDAO.findAllByClusterIdAndMarket(clusterId.getId(), market));
    }

    public Optional<ProviderConfiguration> getProviderByName(Locale locale, ClusterId clusterId, String providerName) {
        try {
            return Optional.of(translate(locale,
                    providerConfigurationDAO.findByClusterIdAndProviderName(clusterId.getId(), providerName)));
        } catch (NoResultException e) {
            log.warn("Could not find providerConfiguration for clusterId: %s, providerName: %s",
                    clusterId.getId(), providerName);
        }
        return Optional.empty();
    }

    private List<ProviderConfiguration> translate(Locale locale, List<ProviderConfiguration> providerConfigurations) {
        return providerConfigurations.stream()
                .map(providerConfiguration -> translate(locale, providerConfiguration))
                .collect(Collectors.toList());
    }

    private ProviderConfiguration translate(Locale locale, ProviderConfiguration providerConfiguration) {
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

    private Field translate(Catalog catalog, Field field) {
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
}
