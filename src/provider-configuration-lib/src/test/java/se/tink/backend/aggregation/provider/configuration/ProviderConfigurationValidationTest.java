package se.tink.backend.aggregation.provider.configuration;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.lang.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfiguration;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Field;
import se.tink.libraries.pair.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ProviderConfigurationValidationTest extends ProviderConfigurationServiceTestBase {
    @Inject
    private @Named("providerConfiguration") Map<String, ProviderConfiguration> providerConfigurationByName;
    @Inject
    private @Named("enabledProvidersOnCluster") Map<String, Set<String>> enabledProvidersOnCluster;
    @Inject
    private @Named("providerOverrideOnCluster") Map<String, Map<String, ProviderConfiguration>> providerOverrideOnCluster;
    @Inject
    private @Named("capabilitiesByAgent") Map<String, Set<ProviderConfiguration.Capability>> providerAgentCapabilities;

    private static final Predicate<ProviderConfiguration> FILTER_OUT_ABSTRACT_AND_ICS_AGENTS = provider ->
            !provider.getName().toLowerCase().contains("abstract");


    @Test
    public void validateAllAvailableProvidersForAClusterAreAvailableInConfigurations() {
        Map<String, List<String>> missingProvidersByClusterId = Maps.newHashMap();

        for (String clusterId : enabledProvidersOnCluster.keySet()) {

            Set<String> providerNamesForCluster = enabledProvidersOnCluster.get(clusterId);
            if (Objects.isNull(providerNamesForCluster) || providerNamesForCluster.isEmpty()) {
                continue;
            }

            List<String> missingProviders = new ArrayList<>();
            providerNamesForCluster.forEach(providerName -> {
                ProviderConfiguration providerConfigurationForCluster = getProviderConfigurationForCluster(
                        clusterId, providerName);

                if (Objects.isNull(providerConfigurationForCluster)) {
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

    private ProviderConfiguration getProviderConfigurationForCluster(String clusterId, String providerName) {
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

            Set<String> providerNamesForCluster = enabledProvidersOnCluster.getOrDefault(
                    clusterId, Collections.emptySet());

            if (providerNamesForCluster.isEmpty()) {
                continue;
            }

            List<String> providersWithMarketNull = providerNamesForCluster.stream()
                    .map(providerName -> getProviderConfigurationForCluster(clusterId, providerName))
                    .filter(Objects::nonNull)
                    .filter(providerConfiguration -> Objects.isNull(providerConfiguration.getMarket()))
                    .map(ProviderConfiguration::getName)
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

            Set<String> providerNamesForCluster = enabledProvidersOnCluster.getOrDefault(
                    clusterId, Collections.emptySet());

            if (providerNamesForCluster.isEmpty()) {
                continue;
            }

            List<String> providersWithMarketNull = providerNamesForCluster.stream()
                    .map(providerName -> getProviderConfigurationForCluster(clusterId, providerName))
                    .filter(Objects::nonNull)
                    .filter(providerConfiguration -> Objects.isNull(providerConfiguration.getCurrency()))
                    .map(ProviderConfiguration::getName)
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
        Set<String> providerCapabilityClassNames = providerConfigurationByName.values().stream()
                .filter(FILTER_OUT_ABSTRACT_AND_ICS_AGENTS)
                .map(ProviderConfiguration::getClassName)
                .collect(Collectors.toSet());

        Set<String> agentCapabilityClassNames = providerAgentCapabilities.keySet().stream().collect(Collectors.toSet());
        agentCapabilityClassNames.removeAll(providerCapabilityClassNames);
        assertThat(agentCapabilityClassNames).isEmpty();
    }

    @Test
    public void verifyAllProviderCapabilitiesAreAvailableInAgentCapabilities() {
        Set<String> providerCapabilityClassNames = providerConfigurationByName.values().stream()
                .filter(FILTER_OUT_ABSTRACT_AND_ICS_AGENTS)
                .map(ProviderConfiguration::getClassName)
                .collect(Collectors.toSet());

        Set<String> agentCapabilityClassNames = providerAgentCapabilities.keySet().stream().collect(Collectors.toSet());
        providerCapabilityClassNames.removeAll(agentCapabilityClassNames);
        assertThat(providerCapabilityClassNames).isEmpty();
    }

    // TODO No need to execute more than once
    private Set<Pair<String, Pair<ProviderConfiguration, Field>>> providerRows() {
        return enabledProvidersOnCluster
                .keySet()
                .stream()
                .map(
                        clusterId ->
                                Pair.of(
                                        clusterId,
                                        enabledProvidersOnCluster.getOrDefault(
                                                clusterId, Collections.emptySet())))
                .flatMap(pair -> pair.second.stream().map(second -> Pair.of(pair.first, second)))
                .map(
                        pair ->
                                Pair.of(
                                        pair.first,
                                        getProviderConfigurationForCluster(
                                                pair.first, pair.second)))
                .flatMap(
                        pair ->
                                pair.second
                                        .getFields()
                                        .stream()
                                        .map(
                                                field ->
                                                        Pair.of(
                                                                pair.first,
                                                                Pair.of(pair.second, field))))
                .collect(Collectors.toSet());
    }

    private static List<String> collectRows(
            Set<Pair<String, Pair<ProviderConfiguration, Field>>> triples) {
        return triples.stream()
                .map(triple -> String.format("%s:%s", triple.first, triple.second.first.getName()))
                .sorted()
                .collect(Collectors.toList());
    }

    /** @throws AssertionError if there exists a provider field that satisfies the predicate */
    private void validateFields(BiPredicate<ProviderConfiguration, Field> isInvalidField) {
        Set<Pair<String, Pair<ProviderConfiguration, Field>>> triples = providerRows();

        Set<Pair<String, Pair<ProviderConfiguration, Field>>> violations =
                triples.stream()
                        .filter(
                                triple ->
                                        isInvalidField.test(
                                                triple.second.first, triple.second.second))
                        .collect(Collectors.toSet());
        List<String> results = collectRows(violations);

        assertThat(results).isEmpty();
    }

    @Ignore("Ignored until we have fixed all 1109 offending providers")
    @Test
    public void validatePasswordFieldsAreSensitive() {
        validateFields(
                (conf, field) ->
                        conf.getCredentialsType() == CredentialsTypes.PASSWORD
                                && Objects.equals(field.getName(), "password")
                                && !field.isSensitive());
    }

    @Ignore("Ignored until we have fixed all 47 offending providers")
    @Test
    public void validatePasswordFieldsAreMasked() {
        validateFields(
                (conf, field) ->
                        conf.getCredentialsType() == CredentialsTypes.PASSWORD
                                && Objects.equals(field.getName(), "password")
                                && !field.isMasked());
    }

    @Ignore("Ignored until we have fixed the two offending providers")
    @Test
    public void validateCardNumberFieldsAreImmutable() {
        validateFields(
                (conf, field) ->
                        StringUtils.equalsIgnoreCase(field.getDescription(), "card number")
                                && !field.isImmutable());
    }

    @Ignore("Ignored until we have fixed the offending provider")
    @Test
    public void validateCardNumberFieldsAreNumeric() {
        validateFields(
                (conf, field) ->
                        StringUtils.equalsIgnoreCase(field.getDescription(), "card number")
                                && !field.isNumeric());
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
