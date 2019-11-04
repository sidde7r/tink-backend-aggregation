package se.tink.backend.aggregation.agents.tools;

import com.google.common.collect.Sets;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.ClientConfiguration;
import se.tink.backend.aggregation.log.AggregationLogger;

public final class ClientConfigurationMetaInfoHandler {
    private static final AggregationLogger log =
            new AggregationLogger(ClientConfigurationMetaInfoHandler.class);
    private static final String AGENTS_PACKAGE_PREFIX = "se.tink.backend.aggregation.agents";

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

        Set<Field> fields =
                Sets.newHashSet(clientConfigurationClassForProvider.getDeclaredFields());

        Set<Field> secretFieldsSet =
                fields.stream().filter(this::isFieldSecret).collect(Collectors.toSet());

        if (secretFieldsSet.stream().anyMatch(this::isFieldSensitiveSecret)) {
            throw new IllegalStateException(
                    "A secret cannot be both non-sensitive and sensitive, revise the annotations in your configuration class.");
        }

        return secretFieldsSet;
    }

    public Set<Field> getSensitiveSecretFields() {
        Class<? extends ClientConfiguration> clientConfigurationClassForProvider =
                findClosestClientConfigurationClass();

        Set<Field> fields =
                Sets.newHashSet(clientConfigurationClassForProvider.getDeclaredFields());

        Set<Field> sensitiveSecretFieldsSet =
                fields.stream().filter(this::isFieldSensitiveSecret).collect(Collectors.toSet());

        if (sensitiveSecretFieldsSet.stream().anyMatch(this::isFieldSecret)) {
            throw new IllegalStateException(
                    "A secret cannot be both non-sensitive and sensitive, revise the annotations in your configuration class.");
        }

        return sensitiveSecretFieldsSet;
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
}
