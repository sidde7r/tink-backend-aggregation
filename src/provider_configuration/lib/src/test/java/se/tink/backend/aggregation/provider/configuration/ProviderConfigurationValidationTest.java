package se.tink.backend.aggregation.provider.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfigurationStorage;
import se.tink.libraries.credentials.enums.CredentialsTypes;
import se.tink.libraries.field.rpc.Field;
import se.tink.libraries.pair.Pair;

public class ProviderConfigurationValidationTest extends ProviderConfigurationServiceTestBase {
    @Inject
    private @Named("providerConfiguration") Map<String, ProviderConfigurationStorage>
            providerConfigurationByName;

    @Inject
    private @Named("enabledProvidersOnCluster") Map<String, Set<String>> enabledProvidersOnCluster;

    @Inject
    private @Named("providerOverrideOnCluster") Map<
                    String, Map<String, ProviderConfigurationStorage>>
            providerOverrideOnCluster;

    @Inject
    private @Named("capabilitiesByAgent") Map<String, Set<ProviderConfigurationStorage.Capability>>
            providerAgentCapabilities;

    private static final Predicate<ProviderConfigurationStorage>
            FILTER_OUT_ABSTRACT_AND_ICS_AGENTS =
                    provider -> !provider.getName().toLowerCase().contains("abstract");

    @Test
    public void validateAllAvailableProvidersForAClusterAreAvailableInConfigurations() {
        Map<String, List<String>> missingProvidersByClusterId = Maps.newHashMap();

        for (String clusterId : enabledProvidersOnCluster.keySet()) {

            Set<String> providerNamesForCluster = enabledProvidersOnCluster.get(clusterId);
            if (Objects.isNull(providerNamesForCluster) || providerNamesForCluster.isEmpty()) {
                continue;
            }

            List<String> missingProviders = new ArrayList<>();
            providerNamesForCluster.forEach(
                    providerName -> {
                        ProviderConfigurationStorage providerConfigurationStorageForCluster =
                                getProviderConfigurationForCluster(clusterId, providerName);

                        if (Objects.isNull(providerConfigurationStorageForCluster)) {
                            missingProviders.add(providerName);
                        }
                    });

            if (missingProviders.isEmpty()) {
                continue;
            }

            missingProvidersByClusterId.put(clusterId, missingProviders);
        }

        assertThat(missingProvidersByClusterId.entrySet()).isEmpty();
    }

    private ProviderConfigurationStorage getProviderConfigurationForCluster(
            String clusterId, String providerName) {
        if (!enabledProvidersOnCluster.containsKey(clusterId)) {
            return null;
        }

        if (!enabledProvidersOnCluster.get(clusterId).contains(providerName)) {
            return null;
        }

        if (!providerOverrideOnCluster.containsKey(clusterId)) {
            return providerConfigurationByName.get(providerName);
        }

        if (!providerOverrideOnCluster.get(clusterId).containsKey(providerName)) {
            return providerConfigurationByName.get(providerName);
        }

        return providerOverrideOnCluster.get(clusterId).get(providerName);
    }

    @Test
    public void validateMarketNotNull() {
        Map<String, List<String>> providersWithMarketNullByClusterId = Maps.newHashMap();

        for (String clusterId : enabledProvidersOnCluster.keySet()) {

            Set<String> providerNamesForCluster =
                    enabledProvidersOnCluster.getOrDefault(clusterId, Collections.emptySet());

            if (providerNamesForCluster.isEmpty()) {
                continue;
            }

            List<String> providersWithMarketNull =
                    providerNamesForCluster.stream()
                            .map(
                                    providerName ->
                                            getProviderConfigurationForCluster(
                                                    clusterId, providerName))
                            .filter(Objects::nonNull)
                            .filter(
                                    providerConfiguration ->
                                            Objects.isNull(providerConfiguration.getMarket()))
                            .map(ProviderConfigurationStorage::getName)
                            .collect(Collectors.toList());

            if (providersWithMarketNull.isEmpty()) {
                continue;
            }

            providersWithMarketNullByClusterId.put(clusterId, providersWithMarketNull);
        }

        assertThat(providersWithMarketNullByClusterId.entrySet()).isEmpty();
    }

    @Test
    public void validateCurrencyNotNull() {
        Map<String, List<String>> providersWithMarketNullByClusterId = Maps.newHashMap();

        for (String clusterId : enabledProvidersOnCluster.keySet()) {

            Set<String> providerNamesForCluster =
                    enabledProvidersOnCluster.getOrDefault(clusterId, Collections.emptySet());

            if (providerNamesForCluster.isEmpty()) {
                continue;
            }

            List<String> providersWithMarketNull =
                    providerNamesForCluster.stream()
                            .map(
                                    providerName ->
                                            getProviderConfigurationForCluster(
                                                    clusterId, providerName))
                            .filter(Objects::nonNull)
                            .filter(
                                    providerConfiguration ->
                                            Objects.isNull(providerConfiguration.getCurrency()))
                            .map(ProviderConfigurationStorage::getName)
                            .collect(Collectors.toList());

            if (providersWithMarketNull.isEmpty()) {
                continue;
            }

            providersWithMarketNullByClusterId.put(clusterId, providersWithMarketNull);
        }

        assertThat(providersWithMarketNullByClusterId.entrySet()).isEmpty();
    }

    @Test
    public void verifyAllAgentCapabilitiesAreAvailableInProviderCapabilities() {
        Set<String> providerCapabilityClassNames =
                providerConfigurationByName.values().stream()
                        .filter(FILTER_OUT_ABSTRACT_AND_ICS_AGENTS)
                        .map(ProviderConfigurationStorage::getClassName)
                        .collect(Collectors.toSet());

        Set<String> agentCapabilityClassNames =
                providerAgentCapabilities.keySet().stream().collect(Collectors.toSet());
        agentCapabilityClassNames.removeAll(providerCapabilityClassNames);
        assertThat(agentCapabilityClassNames).isEmpty();
    }

    @Test
    public void verifyAllProviderCapabilitiesAreAvailableInAgentCapabilities() {
        Set<String> providerCapabilityClassNames =
                providerConfigurationByName.values().stream()
                        .filter(FILTER_OUT_ABSTRACT_AND_ICS_AGENTS)
                        .map(ProviderConfigurationStorage::getClassName)
                        .collect(Collectors.toSet());

        Set<String> agentCapabilityClassNames =
                providerAgentCapabilities.keySet().stream().collect(Collectors.toSet());
        providerCapabilityClassNames.removeAll(agentCapabilityClassNames);
        assertThat(providerCapabilityClassNames).isEmpty();
    }

    // TODO No need to execute more than once
    private Set<Pair<String, Pair<ProviderConfigurationStorage, Field>>> providerRows() {
        Function<String, Pair<String, Set<String>>> clusterToClusterAndProviderStrings =
                clusterId ->
                        Pair.of(
                                clusterId,
                                enabledProvidersOnCluster.getOrDefault(
                                        clusterId, Collections.emptySet()));

        Function<Pair<String, String>, Pair<String, ProviderConfigurationStorage>>
                stringToProviderConfig =
                        pair ->
                                Pair.of(
                                        pair.first,
                                        getProviderConfigurationForCluster(
                                                pair.first, pair.second));

        return enabledProvidersOnCluster.keySet().stream()
                .map(clusterToClusterAndProviderStrings)
                .flatMap(pair -> pair.second.stream().map(second -> Pair.of(pair.first, second)))
                .map(stringToProviderConfig)
                .flatMap(
                        pair ->
                                pair.second.getFields().stream()
                                        .map(
                                                field ->
                                                        Pair.of(
                                                                pair.first,
                                                                Pair.of(pair.second, field))))
                .collect(Collectors.toSet());
    }

    private static List<String> collectRows(
            Set<Pair<String, Pair<ProviderConfigurationStorage, Field>>> triples) {
        return triples.stream()
                .map(triple -> String.format("%s:%s", triple.first, triple.second.first.getName()))
                .sorted()
                .collect(Collectors.toList());
    }

    /** @throws AssertionError if there exists a provider field that satisfies the predicate */
    private void validateFields(BiPredicate<ProviderConfigurationStorage, Field> isInvalidField) {
        Set<Pair<String, Pair<ProviderConfigurationStorage, Field>>> triples = providerRows();

        Set<Pair<String, Pair<ProviderConfigurationStorage, Field>>> violations =
                triples.stream()
                        .filter(
                                triple ->
                                        isInvalidField.test(
                                                triple.second.first, triple.second.second))
                        .collect(Collectors.toSet());
        List<String> results = collectRows(violations);

        assertThat(results).isEmpty();
    }

    private static boolean isCardNumberField(Field field) {
        return StringUtils.equalsIgnoreCase(field.getName(), "cardid")
                && StringUtils.equalsIgnoreCase(field.getDescription(), "card number");
    }

    @Test
    public void validatePasswordFieldsAreSensitive() {
        validateFields(
                (conf, field) ->
                        conf.getCredentialsType() == CredentialsTypes.PASSWORD
                                && Objects.equals(field.getName(), "password")
                                && !field.isSensitive());
    }

    @Test
    public void validatePasswordFieldsAreMasked() {
        validateFields(
                (conf, field) ->
                        conf.getCredentialsType() == CredentialsTypes.PASSWORD
                                && Objects.equals(field.getName(), "password")
                                && !field.isMasked());
    }

    @Test
    public void validateCardNumberFieldsAreSensitive() {
        validateFields((conf, field) -> isCardNumberField(field) && !field.isSensitive());
    }

    @Test
    public void validateCardNumberFieldsAreImmutable() {
        validateFields((conf, field) -> isCardNumberField(field) && !field.isImmutable());
    }

    @Test
    public void validateCardNumberFieldsAreNumeric() {
        validateFields((conf, field) -> isCardNumberField(field) && !field.isNumeric());
    }

    @Test
    public void validateMinLengthLessThanOrEqualToMaxLength() {
        validateFields(
                (conf, field) ->
                        field.getMinLength() != null
                                && field.getMaxLength() != null
                                && field.getMinLength() > field.getMaxLength());
    }
}
