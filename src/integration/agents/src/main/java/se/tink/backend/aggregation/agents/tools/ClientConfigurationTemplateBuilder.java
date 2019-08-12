package se.tink.backend.aggregation.agents.tools;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringEscapeUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.ClientConfiguration;
import se.tink.backend.aggregation.log.AggregationLogger;

public class ClientConfigurationTemplateBuilder {

    private static final AggregationLogger log =
            new AggregationLogger(ClientConfigurationTemplateBuilder.class);
    private static final String AGENTS_PACKAGE_PREFIX = "se.tink.backend.aggregation.agents";
    private static final int NUM_SPACES_INDENT = 10;
    private static final String PRETTY_PRINTING_INDENT_PADDING =
            new String(new char[NUM_SPACES_INDENT]).replace((char) 0, ' ');

    private final Supplier<IllegalStateException> noConfigurationClassFoundExceptionSupplier;
    private final Provider provider;
    private final String financialInstitutionId;

    public ClientConfigurationTemplateBuilder(Provider provider) {
        this.provider = provider;
        this.financialInstitutionId = provider.getFinancialInstitutionId();
        this.noConfigurationClassFoundExceptionSupplier =
                () ->
                        new IllegalStateException(
                                "No suitable subclass of ClientConfiguration class found for the specified provider : "
                                        + provider.getName());
    }

    public String buildTemplate() {
        Class<? extends ClientConfiguration> clientConfigurationClassForProvider =
                findClosestClientConfigurationClass(provider.getClassName());

        JsonObject jsonTemplate =
                assembleTemplateForConfigurationClass(clientConfigurationClassForProvider);

        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(jsonTemplate);

        return replaceUnwantedCharacters(jsonString);
    }

    private String replaceUnwantedCharacters(String jsonString) {
        return StringEscapeUtils.unescapeJava(
                jsonString.replace("\\n", "\n" + PRETTY_PRINTING_INDENT_PADDING));
    }

    private JsonObject assembleTemplateForConfigurationClass(
            Class<? extends ClientConfiguration> clientConfigurationClassForProvider) {
        JsonObject jsonConfigurationTemplate = new JsonObject();

        JsonObject jsonTemplateForFinancialInstitution = new JsonObject();
        jsonConfigurationTemplate.add(financialInstitutionId, jsonTemplateForFinancialInstitution);

        List<Field> fields = Arrays.asList(clientConfigurationClassForProvider.getDeclaredFields());
        JsonObject jsonSecrets = getSecretFields(fields);
        JsonObject jsonSensitive = getSensitiveFields(fields);
        jsonTemplateForFinancialInstitution.add("secrets", jsonSecrets);
        jsonTemplateForFinancialInstitution.add("sensitive", jsonSensitive);

        return jsonConfigurationTemplate;
    }

    private JsonObject getSensitiveFields(List<Field> fields) {
        JsonObject sensitiveFieldsJson = new JsonObject();

        List<Field> sensitiveFieldsList =
                fields.stream().filter(this::isFieldSensitiveSecret).collect(Collectors.toList());

        if (sensitiveFieldsList.stream().anyMatch(this::isFieldSecret)) {
            throw new IllegalStateException(
                    "A secret cannot be both non-sensitive and sensitive, revise the annotations in your configuration class");
        }

        sensitiveFieldsList.stream()
                .forEach(
                        sensitiveField ->
                                sensitiveFieldsJson.addProperty(
                                        sensitiveField.getName(),
                                        getDescriptionAndExample(sensitiveField)));

        return sensitiveFieldsJson;
    }

    private JsonObject getSecretFields(List<Field> fields) {
        JsonObject secretFieldsJson = new JsonObject();

        List<Field> secretFieldsList =
                fields.stream().filter(this::isFieldSecret).collect(Collectors.toList());

        if (secretFieldsList.stream().anyMatch(this::isFieldSensitiveSecret)) {
            throw new IllegalStateException(
                    "A secret cannot be both non-sensitive and sensitive, revise the annotations in your configuration class");
        }

        secretFieldsList.stream()
                .forEach(
                        secretField ->
                                secretFieldsJson.addProperty(
                                        secretField.getName(),
                                        getDescriptionAndExample(secretField)));

        return secretFieldsJson;
    }

    private String getDescriptionAndExample(Field field) {
        List<Method> candidateMethods = getCandidateMethodsDescriptionExample(field);

        Optional<Method> descriptionMethod =
                candidateMethods.stream()
                        .filter(
                                method ->
                                        isMethodForFieldWithPrefix(
                                                method, field.getName(), "getDescription"))
                        .findFirst();

        Optional<Method> exampleMethod =
                candidateMethods.stream()
                        .filter(
                                method ->
                                        isMethodForFieldWithPrefix(
                                                method, field.getName(), "getExample"))
                        .findFirst();

        StringBuffer sb = new StringBuffer();
        if (descriptionMethod.isPresent()) {
            try {
                sb.append(System.lineSeparator());
                sb.append("Description:");
                sb.append(System.lineSeparator());
                sb.append(prettifyContentString((String) descriptionMethod.get().invoke(null)));
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.debug(
                        "Could not add description, check that your description method has the right signature : "
                                + getRightMethodSignature(descriptionMethod.get()));
            }
        }

        if (exampleMethod.isPresent()) {
            try {
                sb.append(System.lineSeparator());
                sb.append("Example:");
                sb.append(System.lineSeparator());
                sb.append(prettifyContentString((String) exampleMethod.get().invoke(null)));
                sb.append(System.lineSeparator());
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.debug(
                        "Could not add example, check that your example method has the right signature : "
                                + getRightMethodSignature(exampleMethod.get()));
            }
        }

        if (sb.length() == 0) {
            sb.append(field.getType().getSimpleName());
        }

        return sb.toString();
    }

    private List<Method> getCandidateMethodsDescriptionExample(Field field) {
        List<Method> allCandidateMethods = new ArrayList<>();

        List<Method> candidateMethodsFromClass =
                filterMethodsBySignature(field.getDeclaringClass().getDeclaredMethods());
        allCandidateMethods.addAll(candidateMethodsFromClass);

        List<Method> candidateMethodsFromInterface =
                filterMethodsBySignature(ClientConfiguration.class.getDeclaredMethods());
        allCandidateMethods.addAll(candidateMethodsFromInterface);

        return allCandidateMethods;
    }

    private List<Method> filterMethodsBySignature(Method[] declaredMethods) {
        Predicate<Method> methodSignatureChecker =
                method ->
                        method.getReturnType().equals(String.class)
                                && method.getParameterCount() == 0;

        return Arrays.stream(declaredMethods)
                .filter(methodSignatureChecker)
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .collect(Collectors.toList());
    }

    private String prettifyContentString(String content) {
        return "  " + content.replace("\n", "\n  ");
    }

    private String getRightMethodSignature(Method method) {
        return "public static String " + method.getName() + "()";
    }

    private boolean isMethodForFieldWithPrefix(Method method, String fieldName, String prefix) {
        String camelCasedFieldName =
                fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

        return method.getName().endsWith(prefix + camelCasedFieldName);
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

    private Class<? extends ClientConfiguration> findClosestClientConfigurationClass(
            String providerClassName) {
        String fullyQualifiedClassName = AGENTS_PACKAGE_PREFIX + "." + providerClassName;

        // We first look into the same package tree
        Optional<Class<? extends ClientConfiguration>> closestConfigurationClass =
                searchForClosestConfigurationClassInSamePackageTree(fullyQualifiedClassName);

        // Otherwise we look among the superclasses of our agent class. This comes in handy
        // when dealing with agents that inherit from service providers or when there is
        // another reason for an agent to share a configuration class with a predecessor.
        if (!closestConfigurationClass.isPresent()) {
            closestConfigurationClass =
                    searchForClosestConfigurationClassAmongSuperclasses(fullyQualifiedClassName);
        }

        return closestConfigurationClass.orElseThrow(noConfigurationClassFoundExceptionSupplier);
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
            log.error("Could not find specified Class for : " + fullyQualifiedClassName, e);
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
}
