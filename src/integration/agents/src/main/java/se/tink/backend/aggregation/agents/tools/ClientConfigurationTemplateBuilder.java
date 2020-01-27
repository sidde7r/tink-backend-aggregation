package se.tink.backend.aggregation.agents.tools;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringEscapeUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

public class ClientConfigurationTemplateBuilder {
    private static final int NUM_SPACES_INDENT = 10;
    private static final String PRETTY_PRINTING_INDENT_PADDING =
            new String(new char[NUM_SPACES_INDENT]).replace((char) 0, ' ');
    private static final String FIN_IDS_KEY = "finId";

    private final boolean includeDescriptions;
    private final boolean includeExamples;
    private final String financialInstitutionId;
    private final ClientConfigurationMetaInfoHandler clientConfigurationMetaInfoHandler;

    public ClientConfigurationTemplateBuilder(
            Provider provider, boolean includeDescriptions, boolean includeExamples) {
        this.includeDescriptions = includeDescriptions;
        this.includeExamples = includeExamples;
        this.financialInstitutionId = provider.getFinancialInstitutionId();
        this.clientConfigurationMetaInfoHandler = new ClientConfigurationMetaInfoHandler(provider);
        Preconditions.checkNotNull(
                Strings.emptyToNull(this.financialInstitutionId),
                "financialInstitutionId in requested provider cannot be null.");
    }

    public String buildTemplate() {
        Class<? extends ClientConfiguration> clientConfigurationClassForProvider =
                clientConfigurationMetaInfoHandler.findClosestClientConfigurationClass();

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

        Map<String, String> fieldsDescriptionsAndExamples =
                readFieldsDescriptionsAndExamples(clientConfigurationClassForProvider);

        JsonObject jsonSecrets = getSecretFields(fieldsDescriptionsAndExamples);
        JsonObject jsonSensitive = getSensitiveFields(fieldsDescriptionsAndExamples);
        JsonObject jsonAgentConfigParam = getAgentConfigParamFields(fieldsDescriptionsAndExamples);
        jsonConfigurationTemplate.add("secrets", jsonSecrets);
        jsonConfigurationTemplate.add("sensitive", jsonSensitive);
        jsonAgentConfigParam.entrySet().stream()
                .forEach(
                        agentConfigParamEntry ->
                                jsonConfigurationTemplate.add(
                                        agentConfigParamEntry.getKey(),
                                        agentConfigParamEntry.getValue()));

        return jsonConfigurationTemplates;
    }

    private JsonObject getSensitiveFields(Map<String, String> fieldsDescriptionsAndExamples) {
        JsonObject sensitiveFieldsJson = new JsonObject();

        Set<Field> sensitiveFieldsSet =
                clientConfigurationMetaInfoHandler.getSensitiveSecretFields();

        sensitiveFieldsSet.stream()
                .forEach(
                        sensitiveField ->
                                sensitiveFieldsJson.addProperty(
                                        clientConfigurationMetaInfoHandler.fieldToFieldName.apply(
                                                sensitiveField),
                                        getDescriptionAndExample(
                                                sensitiveField, fieldsDescriptionsAndExamples)));

        return sensitiveFieldsJson;
    }

    private JsonObject getSecretFields(Map<String, String> fieldsDescriptionsAndExamples) {
        JsonObject secretFieldsJson = new JsonObject();

        Set<Field> secretFieldsSet = clientConfigurationMetaInfoHandler.getSecretFields();

        secretFieldsSet.stream()
                .forEach(
                        secretField ->
                                secretFieldsJson.addProperty(
                                        clientConfigurationMetaInfoHandler.fieldToFieldName.apply(
                                                secretField),
                                        getDescriptionAndExample(
                                                secretField, fieldsDescriptionsAndExamples)));

        return secretFieldsJson;
    }

    private JsonObject getAgentConfigParamFields(
            Map<String, String> fieldsDescriptionsAndExamples) {
        JsonObject agentConfigParamJson = new JsonObject();

        Set<Field> agentConfigParamFieldsSet =
                clientConfigurationMetaInfoHandler.getAgentConfigParamFields();

        for (Field field : agentConfigParamFieldsSet) {
            String descriptionAndExample =
                    getDescriptionAndExample(field, fieldsDescriptionsAndExamples);
            try {
                List<String> examples = (new Gson()).fromJson(descriptionAndExample, List.class);
                JsonArray examplesJson = new JsonArray();
                examples.stream().forEach(example -> examplesJson.add(new JsonPrimitive(example)));
                agentConfigParamJson.add(
                        clientConfigurationMetaInfoHandler.fieldToFieldName.apply(field),
                        examplesJson);
            } catch (Exception e) {
                agentConfigParamJson.addProperty(
                        clientConfigurationMetaInfoHandler.fieldToFieldName.apply(field),
                        descriptionAndExample);
            }
        }

        return agentConfigParamJson;
    }

    private String getDescriptionAndExample(
            Field field, Map<String, String> fieldsDescriptionsAndExamples) {
        String fieldName = clientConfigurationMetaInfoHandler.fieldToFieldName.apply(field);

        StringBuilder sb = new StringBuilder();

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
}
