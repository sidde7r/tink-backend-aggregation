package se.tink.backend.aggregation.agents.tools;

import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringEscapeUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
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
    private static final ImmutableMap<String, String> specialFieldsMapper =
            new ImmutableMap.Builder<String, String>().put("redirectUrl", "redirectUrls").build();
    private static final String FIN_IDS_KEY = "finId";

    private final Supplier<IllegalArgumentException> noConfigurationClassFoundExceptionSupplier;
    private final Provider provider;
    private final boolean includeDescriptions;
    private final boolean includeExamples;
    private final String financialInstitutionId;
    private final Function<Field, String> fieldToFieldName =
            field -> specialFieldsMapper.getOrDefault(field.getName(), field.getName());

    public ClientConfigurationTemplateBuilder(
            Provider provider, boolean includeDescriptions, boolean includeExamples) {
        this.provider = provider;
        this.includeDescriptions = includeDescriptions;
        this.includeExamples = includeExamples;
        this.financialInstitutionId = provider.getFinancialInstitutionId();
        this.noConfigurationClassFoundExceptionSupplier =
                () ->
                        new IllegalArgumentException(
                                "No suitable subclass of ClientConfiguration class found for the specified provider : "
                                        + provider.getName());
    }

    public String buildTemplate() {
        Class<? extends ClientConfiguration> clientConfigurationClassForProvider =
                findClosestClientConfigurationClass(provider.getClassName());

        JsonArray jsonTemplate =
                assembleTemplateForConfigurationClass(clientConfigurationClassForProvider);

        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(jsonTemplate);

        return replaceUnwantedCharacters(jsonString);
    }

    private Map<String, String> readFieldsDescriptionsAndExamples(
            Class<? extends ClientConfiguration> clientConfigurationClassForProvider) {

        Yaml yaml = new Yaml();

        // First we retrieve the common descriptions and examples.
        String fieldsDescriptionsAndExamplesCommonPath = "config-templates/Common.yaml";

        InputStream fieldsDescriptionsAndExamplesCommonStream =
                Optional.ofNullable(
                                clientConfigurationClassForProvider
                                        .getClassLoader()
                                        .getResourceAsStream(
                                                fieldsDescriptionsAndExamplesCommonPath))
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Could not find 'config-templates/Common.yaml', make sure it is included as a resource."));

        final Map<String, String> fieldsDescriptionAndExamplesCommon;
        try {
            fieldsDescriptionAndExamplesCommon =
                    (Map<String, String>) yaml.load(fieldsDescriptionsAndExamplesCommonStream);
        } catch (YAMLException e) {
            throw new IllegalStateException(
                    "Problem when loading the common descriptions and examples template yaml file: "
                            + fieldsDescriptionsAndExamplesCommonPath,
                    e);
        }

        // Now we get the provider specific descriptions and examples, if there are any duplicates
        // from the common ones, the provider specific ones will override the common ones.
        String fieldsDescriptionsAndExamplesForProviderPath =
                "config-templates/"
                        + clientConfigurationClassForProvider.getCanonicalName().replace(".", "/")
                        + ".yaml";

        InputStream fieldsDescriptionsAndExamplesForProviderStream =
                clientConfigurationClassForProvider
                        .getClassLoader()
                        .getResourceAsStream(fieldsDescriptionsAndExamplesForProviderPath);

        if (fieldsDescriptionsAndExamplesForProviderStream != null) {
            final Map<String, String> fieldsDescriptionAndExamplesForProvider;
            try {
                fieldsDescriptionAndExamplesForProvider =
                        (Map<String, String>)
                                yaml.load(fieldsDescriptionsAndExamplesForProviderStream);
            } catch (YAMLException e) {
                throw new IllegalArgumentException(
                        "Problem when loading yaml file: "
                                + fieldsDescriptionsAndExamplesForProviderPath,
                        e);
            }

            // Merge the common and the provider specific ones.
            fieldsDescriptionAndExamplesCommon.putAll(fieldsDescriptionAndExamplesForProvider);
        }

        return fieldsDescriptionAndExamplesCommon;
    }

    private String replaceUnwantedCharacters(String jsonString) {
        return StringEscapeUtils.unescapeJava(
                jsonString.replace("\\n", "\n" + PRETTY_PRINTING_INDENT_PADDING));
    }

    private JsonArray assembleTemplateForConfigurationClass(
            Class<? extends ClientConfiguration> clientConfigurationClassForProvider) {
        JsonArray jsonConfigurationTemplates = new JsonArray();

        JsonObject jsonConfigurationTemplate = new JsonObject();
        jsonConfigurationTemplates.add(jsonConfigurationTemplate);

        JsonArray jsonFinancialInstitutionIdsArray = new JsonArray();
        jsonConfigurationTemplate.add(FIN_IDS_KEY, jsonFinancialInstitutionIdsArray);
        jsonFinancialInstitutionIdsArray.add(new JsonPrimitive(financialInstitutionId));

        List<Field> fields = Arrays.asList(clientConfigurationClassForProvider.getDeclaredFields());

        Map<String, String> fieldsDescriptionsAndExamples =
                readFieldsDescriptionsAndExamples(clientConfigurationClassForProvider);

        JsonObject jsonSecrets = getSecretFields(fields, fieldsDescriptionsAndExamples);
        JsonObject jsonSensitive = getSensitiveFields(fields, fieldsDescriptionsAndExamples);
        jsonConfigurationTemplate.add("secrets", jsonSecrets);
        jsonConfigurationTemplate.add("sensitive", jsonSensitive);

        return jsonConfigurationTemplates;
    }

    private JsonObject getSensitiveFields(
            List<Field> fields, Map<String, String> fieldsDescriptionsAndExamples) {
        JsonObject sensitiveFieldsJson = new JsonObject();

        List<Field> sensitiveFieldsList =
                fields.stream().filter(this::isFieldSensitiveSecret).collect(Collectors.toList());

        if (sensitiveFieldsList.stream().anyMatch(this::isFieldSecret)) {
            throw new IllegalStateException(
                    "A secret cannot be both non-sensitive and sensitive, revise the annotations in your configuration class.");
        }

        sensitiveFieldsList.stream()
                .forEach(
                        sensitiveField ->
                                sensitiveFieldsJson.addProperty(
                                        fieldToFieldName.apply(sensitiveField),
                                        getDescriptionAndExample(
                                                sensitiveField, fieldsDescriptionsAndExamples)));

        return sensitiveFieldsJson;
    }

    private JsonObject getSecretFields(
            List<Field> fields, Map<String, String> fieldsDescriptionsAndExamples) {
        JsonObject secretFieldsJson = new JsonObject();

        List<Field> secretFieldsList =
                fields.stream().filter(this::isFieldSecret).collect(Collectors.toList());

        if (secretFieldsList.stream().anyMatch(this::isFieldSensitiveSecret)) {
            throw new IllegalStateException(
                    "A secret cannot be both non-sensitive and sensitive, revise the annotations in your configuration class.");
        }

        secretFieldsList.stream()
                .forEach(
                        secretField ->
                                secretFieldsJson.addProperty(
                                        fieldToFieldName.apply(secretField),
                                        getDescriptionAndExample(
                                                secretField, fieldsDescriptionsAndExamples)));

        return secretFieldsJson;
    }

    private String getDescriptionAndExample(
            Field field, Map<String, String> fieldsDescriptionsAndExamples) {
        String fieldName = fieldToFieldName.apply(field);

        StringBuffer sb = new StringBuffer();

        if (includeDescriptions) {
            String fieldDescriptionKey = fieldName + "-description";
            if (fieldsDescriptionsAndExamples.containsKey(fieldDescriptionKey)) {
                sb.append(System.lineSeparator());
                sb.append("Description:");
                sb.append(System.lineSeparator());
                sb.append(fieldsDescriptionsAndExamples.get(fieldDescriptionKey));
            }
        }

        if (includeExamples) {
            String fieldExampleKey = fieldName + "-example";
            if (fieldsDescriptionsAndExamples.containsKey(fieldExampleKey)) {
                String example = fieldsDescriptionsAndExamples.get(fieldExampleKey);
                if (includeDescriptions) {
                    sb.append(System.lineSeparator());
                    sb.append("Example:");
                    sb.append(System.lineSeparator());
                } else {
                    example = example.trim();
                    if (example.startsWith("\"") && example.endsWith("\"")) {
                        example = example.substring(1, example.length() - 1);
                    }
                }
                sb.append(example);
            }
        }

        if (sb.length() == 0) {
            sb.append(field.getType().getSimpleName());
        }

        return sb.toString();
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
}
