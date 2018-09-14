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
import se.tink.backend.aggregation.provider.configuration.rpc.ProviderConfigurationDTO;
import se.tink.backend.core.Field;
import se.tink.libraries.i18n.Catalog;

public class ProviderServiceController {
    private static final Logger log = LoggerFactory.getLogger(ProviderServiceController.class);

    private final ProviderConfigurationProvider providerConfigurationProvider;

    @Inject
    public ProviderServiceController(ProviderConfigurationProvider providerConfigurationProvider) {
        this.providerConfigurationProvider = providerConfigurationProvider;
    }

    public List<ProviderConfigurationDTO> list(Locale locale) {
        return translate(locale, providerConfigurationProvider.findAll());
    }

    public List<ProviderConfigurationDTO> list(Locale locale, ClusterId clusterId) {
        return translate(locale, providerConfigurationProvider.findAllByClusterId(clusterId.getId()));
    }

    public List<ProviderConfigurationDTO> listByMarket(Locale locale, ClusterId clusterId, String market) {
        return translate(locale, providerConfigurationProvider.findAllByClusterIdAndMarket(clusterId.getId(), market));
    }

    public List<ProviderConfigurationDTO> listByMarket(Locale locale, String market) {
        return translate(locale, providerConfigurationProvider.findAllByMarket(market));
    }

    public Optional<ProviderConfigurationDTO> getProviderByName(Locale locale, ClusterId clusterId, String providerName) {
        try {
            return Optional.of(translate(locale,
                    providerConfigurationProvider.findByClusterIdAndProviderName(clusterId.getId(), providerName)));
        } catch (NoResultException e) {
            log.warn("Could not find providerConfiguration for clusterId: %s, providerName: %s",
                    clusterId.getId(), providerName);
        }
        return Optional.empty();
    }

    public Optional<ProviderConfigurationDTO> getProviderByName(Locale locale, String providerName) {
        try {
            return Optional.of(translate(locale, providerConfigurationProvider.findByName(providerName)));
        } catch (NoResultException e) {
            log.warn("Could not find providerConfiguration for providerName: %s", providerName);
        }
        return Optional.empty();
    }

    private List<ProviderConfigurationDTO> translate(Locale locale, List<ProviderConfigurationDTO> providerConfigurations) {
        return providerConfigurations.stream()
                .map(providerConfiguration -> translate(locale, providerConfiguration))
                .collect(Collectors.toList());
    }

    private ProviderConfigurationDTO translate(Locale locale, ProviderConfigurationDTO providerConfiguration) {
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
