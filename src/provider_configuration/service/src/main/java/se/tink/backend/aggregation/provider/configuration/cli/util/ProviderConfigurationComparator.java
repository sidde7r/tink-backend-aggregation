package se.tink.backend.aggregation.provider.configuration.cli.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfigurationStorage;
import se.tink.libraries.field.rpc.Field;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProviderConfigurationComparator {

    private static final Logger log = LoggerFactory.getLogger(ProviderConfigurationComparator.class);
    private static boolean equals = true;

    public static boolean equals(final ProviderConfigurationStorage providerConfigurationStorage1,
                                 final ProviderConfigurationStorage providerConfigurationStorage2) {
        compareProvider(providerConfigurationStorage1, providerConfigurationStorage2);
        return equals;
    }

    private static void compareProvider(final ProviderConfigurationStorage localProvider, final ProviderConfigurationStorage clusterProvider) {
        final String providerName = localProvider.getName();

        if (!Objects.equals(localProvider.getClassName(), clusterProvider.getClassName())) {
            log.info("[{}] [provider] className - Local: {}, Cluster: {}", providerName,
                    localProvider.getClassName(), clusterProvider.getClassName());
            equals = false;
        }

        if (!Objects.equals(localProvider.getCredentialsType(), clusterProvider.getCredentialsType())) {
            log.info("[{}] [provider] credentialsType - Local: {}, Cluster: {}", providerName,
                    localProvider.getCredentialsType(), clusterProvider.getCredentialsType());
            equals = false;
        }

        if (!Objects.equals(localProvider.getCurrency(), clusterProvider.getCurrency())) {
            log.info("[{}] [provider] currency - Local: {}, Cluster: {}", providerName,
                    localProvider.getCurrency(), clusterProvider.getCurrency());
            equals = false;
        }

        if (!Objects.equals(localProvider.getDisplayName(), clusterProvider.getDisplayName())) {
            log.info("[{}] [provider] displayName - Local: {}, Cluster: {}", providerName,
                    localProvider.getDisplayName(), clusterProvider.getDisplayName());
            equals = false;
        }

        if (!Objects.equals(localProvider.getGroupDisplayName(), clusterProvider.getGroupDisplayName())) {
            log.info("[{}] [provider] groupDisplayName - Local: {}, Cluster: {}", providerName,
                    localProvider.getGroupDisplayName(), clusterProvider.getGroupDisplayName());
            equals = false;
        }

        if (!Objects.equals(localProvider.getMarket(), clusterProvider.getMarket())) {
            log.info("[{}] [provider] market - Local: {}, Cluster: {}", providerName,
                    localProvider.getMarket(), clusterProvider.getMarket());
            equals = false;
        }

        if (!Objects.equals(localProvider.isMultiFactor(), clusterProvider.isMultiFactor())) {
            log.info("[{}] [provider] multifactor - Local: {}, Cluster: {}", providerName,
                    localProvider.isMultiFactor(), clusterProvider.isMultiFactor());
            equals = false;
        }

        if (!Objects.equals(localProvider.getPasswordHelpText(), clusterProvider.getPasswordHelpText())) {
            log.info("[{}] [provider] passwordHelpText - Local: {}, Cluster: {}", providerName,
                    localProvider.getPasswordHelpText(), clusterProvider.getPasswordHelpText());
            equals = false;
        }

        if (!Objects.equals(localProvider.getPayload(), clusterProvider.getPayload())) {
            log.info("[{}] [provider] payload - Local: {}, Cluster: {}", providerName,
                    localProvider.getPayload(), clusterProvider.getPayload());
            equals = false;
        }

        if (!Objects.equals(localProvider.isPopular(), clusterProvider.isPopular())) {
            log.info("[{}] [provider] popular - Local: {}, Cluster: {}", providerName,
                    localProvider.isPopular(), clusterProvider.isPopular());
            equals = false;
        }

        if (!Objects.equals(localProvider.getStatus(), clusterProvider.getStatus())) {
            log.info("[{}] [provider] status - Local: {}, Cluster: {}", providerName,
                    localProvider.getStatus(), clusterProvider.getStatus());
            equals = false;
        }

        if (!Objects.equals(localProvider.isTransactional(), clusterProvider.isTransactional())) {
            log.info("[{}] [provider] transactional - Local: {}, Cluster: {}", providerName,
                    localProvider.isTransactional(), clusterProvider.isTransactional());
            equals = false;
        }

        if (!Objects.equals(localProvider.getType(), clusterProvider.getType())) {
            log.info("[{}] [provider] type - Local: {}, Cluster: {}", providerName,
                    localProvider.getType(), clusterProvider.getType());
            equals = false;
        }

        if (!Objects.equals(localProvider.getDisplayDescription(), clusterProvider.getDisplayDescription())) {
            log.info("[{}] [provider] displayDescription - Local: {}, Cluster: {}", providerName,
                    localProvider.getDisplayDescription(), clusterProvider.getDisplayDescription());
            equals = false;
        }

        if (!Objects.equals(localProvider.getRefreshFrequency(), clusterProvider.getRefreshFrequency())) {
            log.warn("[{}] [provider] RefreshFrequency - Local: {}, Cluster: {}", providerName,
                    localProvider.getRefreshFrequency(), clusterProvider.getRefreshFrequency());
        }

        if (!Objects.equals(localProvider.getRefreshFrequencyFactor(), clusterProvider.getRefreshFrequencyFactor())) {
            log.warn("[{}] [provider] RefreshFrequencyFactor - Local: {}, Cluster: {}", providerName,
                    localProvider.getRefreshFrequencyFactor(), clusterProvider.getRefreshFrequencyFactor());
        }

        compareFields(
                providerName,
                localProvider.getFields().stream()
                        .collect(Collectors.toMap(Field::getName, Function.identity())),
                clusterProvider.getFields().stream()
                        .collect(Collectors.toMap(Field::getName, Function.identity())));
    }

    private static void compareFields(String providerName, Map<String, Field> localFields,
                                      Map<String, Field> clusterFields) {
        ImmutableSet<String> differenceInAvailableFields = Sets.difference(
                localFields.keySet(), clusterFields.keySet()).immutableCopy();

        ImmutableSet<String> onlyLocally = Sets.intersection(
                localFields.keySet(), differenceInAvailableFields).immutableCopy();

        if (!onlyLocally.isEmpty()) {
            log.info("[{}] These fields are only available locally {}", providerName, onlyLocally);
            equals = false;
        }

        ImmutableSet<String> onlyCluster = Sets.intersection(
                clusterFields.keySet(), differenceInAvailableFields).immutableCopy();

        if (!onlyLocally.isEmpty()) {
            log.info("[{}] These fields are only available on cluster {}", providerName, onlyCluster);
            equals = false;
        }

        ImmutableSet<String> availableOnBoth = Sets.intersection(
                localFields.keySet(), clusterFields.keySet()).immutableCopy();

        availableOnBoth.forEach(fieldName ->
                compareField(providerName, localFields.get(fieldName), clusterFields.get(fieldName)));
    }

    private static void compareField(String providerName, Field localField, Field clusterField) {
        if (!Objects.equals(localField.getDefaultValue(), clusterField.getDefaultValue())) {
            log.info("[{}] [field] defaultValue - Local: {}, Cluster: {}", providerName,
                    localField.getDefaultValue(), clusterField.getDefaultValue());
            equals = false;
        }

        if (!Objects.equals(localField.getDescription(), clusterField.getDescription())) {
            log.info("[{}] [field] description - Local: {}, Cluster: {}", providerName,
                    localField.getDescription(), clusterField.getDescription());
            equals = false;
        }

        if (!Objects.equals(localField.isExposed(), clusterField.isExposed())) {
            log.info("[{}] [field] exposed - Local: {}, Cluster: {}", providerName,
                    localField.isExposed(), clusterField.isExposed());
            equals = false;
        }

        if (!Objects.equals(localField.getHelpText(), clusterField.getHelpText())) {
            log.info("[{}] [field] helpText - Local: {}, Cluster: {}", providerName,
                    localField.getHelpText(), clusterField.getHelpText());
            equals = false;
        }

        if (!Objects.equals(localField.getHint(), clusterField.getHint())) {
            log.info("[{}] [field] hint - Local: {}, Cluster: {}", providerName,
                    localField.getHint(), clusterField.getHint());
            equals = false;
        }

        if (!Objects.equals(localField.isImmutable(), clusterField.isImmutable())) {
            log.info("[{}] [field] immutable - Local: {}, Cluster: {}", providerName,
                    localField.isImmutable(), clusterField.isImmutable());
            equals = false;
        }

        if (!Objects.equals(localField.isMasked(), clusterField.isMasked())) {
            log.info("[{}] [field] masked - Local: {}, Cluster: {}", providerName,
                    localField.isMasked(), clusterField.isMasked());
            equals = false;
        }

        if (!Objects.equals(localField.getMaxLength(), clusterField.getMaxLength())) {
            log.info("[{}] [field] maxLength - Local: {}, Cluster: {}", providerName,
                    localField.getMaxLength(), clusterField.getMaxLength());
            equals = false;
        }

        if (!Objects.equals(localField.getMinLength(), clusterField.getMinLength())) {
            log.info("[{}] [field] minLength - Local: {}, Cluster: {}", providerName,
                    localField.getMinLength(), clusterField.getMinLength());
            equals = false;
        }

        if (!Objects.equals(localField.getName(), clusterField.getName())) {
            log.info("[{}] [field] name - Local: {}, Cluster: {}", providerName,
                    localField.getName(), clusterField.getName());
            equals = false;
        }

        if (!Objects.equals(localField.isOptional(), clusterField.isOptional())) {
            log.info("[{}] [field] optional - Local: {}, Cluster: {}", providerName,
                    localField.isOptional(), clusterField.isOptional());
            equals = false;
        }

        if (!Objects.equals(localField.getPattern(), clusterField.getPattern())) {
            log.info("[{}] [field] pattern - Local: {}, Cluster: {}", providerName,
                    localField.getPattern(), clusterField.getPattern());
            equals = false;
        }

        if (!Objects.equals(localField.getPatternError(), clusterField.getPatternError())) {
            log.info("[{}] [field] patternError - Local: {}, Cluster: {}", providerName,
                    localField.getPatternError(), clusterField.getPatternError());
            equals = false;
        }

        if (!Objects.equals(localField.getType(), clusterField.getType())) {
            log.info("[{}] [field] type - Local: {}, Cluster: {}", providerName,
                    localField.getType(), clusterField.getType());
            equals = false;
        }

        if (!Objects.equals(localField.getValue(), clusterField.getValue())) {
            log.info("[{}] [field] value - Local: {}, Cluster: {}", providerName,
                    localField.getValue(), clusterField.getValue());
            equals = false;
        }

        if (!Objects.equals(localField.isSensitive(), clusterField.isSensitive())) {
            log.info("[{}] [field] sensitive - Local: {}, Cluster: {}", providerName,
                    localField.isSensitive(), clusterField.isSensitive());
            equals = false;
        }

        if (!Objects.equals(localField.isCheckbox(), clusterField.isCheckbox())) {
            log.info("[{}] [field] checkbox - Local: {}, Cluster: {}", providerName,
                    localField.isCheckbox(), clusterField.isCheckbox());
            equals = false;
        }
    }
}
