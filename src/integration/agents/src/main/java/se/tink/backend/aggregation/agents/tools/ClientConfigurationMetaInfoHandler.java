package se.tink.backend.aggregation.agents.tools;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

public class ClientConfigurationMetaInfoHandler {
    private static final Logger log =
            LoggerFactory.getLogger(ClientConfigurationMetaInfoHandler.class);
    private static final String AGENTS_PACKAGE_PREFIX = "se.tink.backend.aggregation.agents";
    private static final ImmutableBiMap<String, String> specialFieldsMapper =
            new ImmutableBiMap.Builder<String, String>().put("redirectUrl", "redirectUrls").build();
    public final Function<Field, String> fieldToFieldName =
            field -> specialFieldsMapper.getOrDefault(field.getName(), field.getName());

    private final Supplier<IllegalArgumentException> noConfigurationClassFoundExceptionSupplier;

    private final Provider provider;

    public ClientConfigurationMetaInfoHandler(Provider provider) {
        this.provider = provider;
        this.noConfigurationClassFoundExceptionSupplier =
                () ->
                        new IllegalArgumentException(
                                "No suitable subclass of ClientConfiguration class found for the specified provider : "
                                        + provider.getName());
    }

    public Set<Field> getSecretFields() {
        Class<? extends ClientConfiguration> clientConfigurationClassForProvider =
                findClosestClientConfigurationClass();

        Set<Field> secretFields =
                Stream.of(clientConfigurationClassForProvider.getDeclaredFields())
                        .filter(this::isFieldSecret)
                        .collect(Collectors.toSet());

        if (secretFields.stream().anyMatch(this::isFieldSensitiveSecret)
                || secretFields.stream().anyMatch(this::isFieldAgentConfigParam)) {
            throw new IllegalStateException(
                    "A secret cannot be in multiple fields, revise the annotations in your configuration class.");
        }

        return secretFields;
    }

    public Set<String> getSecretFieldsNames() {
        return getSecretFields().stream().map(Field::getName).collect(Collectors.toSet());
    }

    public Set<Field> getSensitiveSecretFields() {
        Class<? extends ClientConfiguration> clientConfigurationClassForProvider =
                findClosestClientConfigurationClass();

        Set<Field> sensitiveSecretFields =
                Stream.of(clientConfigurationClassForProvider.getDeclaredFields())
                        .filter(this::isFieldSensitiveSecret)
                        .collect(Collectors.toSet());

        if (sensitiveSecretFields.stream().anyMatch(this::isFieldSecret)
                || sensitiveSecretFields.stream().anyMatch(this::isFieldAgentConfigParam)) {
            throw new IllegalStateException(
                    "A secret cannot be in multiple fields, revise the annotations in your configuration class.");
        }

        return sensitiveSecretFields;
    }

    public Set<String> getSensitiveSecretFieldsNames() {
        return getSensitiveSecretFields().stream().map(Field::getName).collect(Collectors.toSet());
    }

    public Set<Field> getAgentConfigParamFields() {
        Class<? extends ClientConfiguration> clientConfigurationClassForProvider =
                findClosestClientConfigurationClass();

        Set<Field> agentConfigParamFields =
                Stream.of(clientConfigurationClassForProvider.getDeclaredFields())
                        .filter(this::isFieldAgentConfigParam)
                        .collect(Collectors.toSet());

        if (agentConfigParamFields.stream().anyMatch(this::isFieldSecret)
                || agentConfigParamFields.stream().anyMatch(this::isFieldSensitiveSecret)) {
            throw new IllegalStateException(
                    "A secret cannot be in multiple fields, revise the annotations in your configuration class.");
        }

        return agentConfigParamFields;
    }

    public Set<String> getAgentConfigParamFieldsNames() {
        return getAgentConfigParamFields().stream().map(Field::getName).collect(Collectors.toSet());
    }

    public Class<? extends ClientConfiguration> findClosestClientConfigurationClass() {
        String providerClassName = provider.getClassName();

        String fullyQualifiedClassName = AGENTS_PACKAGE_PREFIX + "." + providerClassName;

        // We first look into the same package tree
        Optional<Class<? extends ClientConfiguration>> closestConfigurationClass =
                searchForClosestConfigurationClassInSamePackageTree(fullyQualifiedClassName);

        // We first look into the same package tree
        return searchForClosestConfigurationClassInSamePackageTree(fullyQualifiedClassName)
                // Otherwise we look among the superclasses of our agent class. This comes in handy
                // when dealing with agents that inherit from service providers or when there is
                // another reason for an agent to share a configuration class with a predecessor.
                .orElseGet(
                        () ->
                                searchForClosestConfigurationClassAmongSuperclasses(
                                                fullyQualifiedClassName)
                                        .orElseThrow(noConfigurationClassFoundExceptionSupplier));
    }

    private Optional<Class<? extends ClientConfiguration>>
            searchForClosestConfigurationClassAmongSuperclasses(
                    final String fullyQualifiedClassName) {
        Class<?> superClassInsideAgentsPackage;
        Optional<Class<? extends ClientConfiguration>> closestConfigurationClass;
        String subclass = fullyQualifiedClassName;
        do {
            superClassInsideAgentsPackage =
                    getSuperClassInsideAgentsPackage(subclass)
                            .orElseThrow(noConfigurationClassFoundExceptionSupplier);
            closestConfigurationClass =
                    searchForClosestConfigurationClassInSamePackageTree(
                            superClassInsideAgentsPackage.getName());
            subclass = superClassInsideAgentsPackage.getName();
        } while (!closestConfigurationClass.isPresent());

        return closestConfigurationClass;
    }

    private Optional<Class<?>> getSuperClassInsideAgentsPackage(
            final String fullyQualifiedClassName) {
        try {
            Class<?> agentClass = Class.forName(fullyQualifiedClassName);
            Class<?> superclass = agentClass.getSuperclass();
            if (superclass.getPackage().getName().startsWith(AGENTS_PACKAGE_PREFIX)) {
                return Optional.of(superclass);
            }
        } catch (ClassNotFoundException e) {
            log.error(
                    "Could not find super class for specified class : " + fullyQualifiedClassName,
                    e);
        }
        return Optional.empty();
    }

    private Optional<Class<? extends ClientConfiguration>>
            searchForClosestConfigurationClassInSamePackageTree(
                    final String fullyQualifiedClassName) {

        int lastIndexPackageSubdivider = fullyQualifiedClassName.lastIndexOf(".");
        String packageToScan = fullyQualifiedClassName.substring(0, lastIndexPackageSubdivider);
        Reflections reflectionsPackageToScan =
                new Reflections(packageToScan, new SubTypesScanner(false));
        Set<Class<? extends ClientConfiguration>> clientConfigurationClassForAgentSet =
                reflectionsPackageToScan.getSubTypesOf(ClientConfiguration.class);

        if (clientConfigurationClassForAgentSet.size() > 1) {
            throw new IllegalStateException(
                    "Found more than one class implementing ClientConfiguration: "
                            + clientConfigurationClassForAgentSet);
        }

        return clientConfigurationClassForAgentSet.stream().findAny();
    }

    private boolean isFieldSecret(Field field) {
        List<Annotation> annotations = Arrays.asList(field.getDeclaredAnnotations());
        return annotations.stream().filter(annotation -> annotation instanceof Secret).count() == 1;
    }

    private boolean isFieldSensitiveSecret(Field field) {
        List<Annotation> annotations = Arrays.asList(field.getDeclaredAnnotations());
        return annotations.stream()
                        .filter(annotation -> annotation instanceof SensitiveSecret)
                        .count()
                == 1;
    }

    private boolean isFieldAgentConfigParam(Field field) {
        List<Annotation> annotations = Arrays.asList(field.getDeclaredAnnotations());
        return annotations.stream()
                        .filter(annotation -> annotation instanceof AgentConfigParam)
                        .count()
                == 1;
    }

    // This maps special parameters that we know won't match (added to the specialFieldsMapper BiMap
    // above) from the ones that are sent in to the ones found in the ClientConfiguration
    // implementing class. An example would be when we send redirectUrls as part of validating the
    // secrets to be uploaded. It should be mapped to redirectUrl as we know.
    public Set<String> mapSpecialConfigClassFieldNames(Set<String> secretsNames) {
        return secretsNames.stream()
                .map(
                        secretNameToValidate ->
                                specialFieldsMapper
                                        .inverse()
                                        .getOrDefault(secretNameToValidate, secretNameToValidate))
                .collect(ImmutableSet.toImmutableSet());
    }

    // This is the inverse of the above function.
    public Set<String> inverseMapSpecialConfigClassFieldNames(Set<String> secretsNames) {
        return secretsNames.stream()
                .map(
                        secretNameToValidate ->
                                specialFieldsMapper.getOrDefault(
                                        secretNameToValidate, secretNameToValidate))
                .collect(ImmutableSet.toImmutableSet());
    }
}
