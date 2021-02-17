package se.tink.backend.aggregation.agents.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringEscapeUtils;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.annotations.Views;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

public class ClientConfigurationTemplateBuilder {
    private static final int NUM_SPACES_INDENT = 10;
    private static final String PRETTY_PRINTING_INDENT_PADDING =
            new String(new char[NUM_SPACES_INDENT]).replace((char) 0, ' ');
    private static final String FIN_IDS_KEY = "finId";
    private static final String PROVIDER_NAME_KEY = "providerId";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_EXAMPLES = "examples";
    private final boolean includeDescriptions;
    private final boolean includeExamples;
    private final String financialInstitutionId;
    private final String providerName;
    private final ClientConfigurationMetaInfoHandler clientConfigurationMetaInfoHandler;

    public ClientConfigurationTemplateBuilder(
            Provider provider, boolean includeDescriptions, boolean includeExamples) {
        this.includeDescriptions = includeDescriptions;
        this.includeExamples = includeExamples;
        this.financialInstitutionId = provider.getFinancialInstitutionId();
        this.providerName = provider.getName();
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
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        mapper.setConfig(mapper.getSerializationConfig().withView(Views.Public.class));
        JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(mapper);
        JsonNode flatSchemaFromConf =
                jsonSchemaGenerator.generateJsonSchema(clientConfigurationClassForProvider);
        JsonNode properties = flatSchemaFromConf.get("properties");
        if (Objects.isNull(properties)) {
            return Collections.emptyMap();
        }
        Iterator<Map.Entry<String, JsonNode>> fields = properties.fields();

        final Map<String, String> fieldsDescriptionAndExamples = new HashMap<>();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            JsonNode fieldValue = properties.get(field.getKey());
            Optional.ofNullable(fieldValue.get(KEY_DESCRIPTION))
                    .map(JsonNode::asText)
                    .ifPresent(
                            text ->
                                    fieldsDescriptionAndExamples.put(
                                            field.getKey() + KEY_DESCRIPTION, text));
            Optional.ofNullable(fieldValue.path(KEY_EXAMPLES))
                    .map(examples -> examples.get(0))
                    .map(JsonNode::asText)
                    .ifPresent(
                            text ->
                                    fieldsDescriptionAndExamples.put(
                                            field.getKey() + KEY_EXAMPLES, text));
        }
        return fieldsDescriptionAndExamples;
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

        JsonArray jsonProviderNamesArray = new JsonArray();
        jsonConfigurationTemplate.add(PROVIDER_NAME_KEY, jsonProviderNamesArray);
        jsonProviderNamesArray.add(new JsonPrimitive(providerName));

        Map<String, String> fieldsDescriptionsAndExamples =
                readFieldsDescriptionsAndExamples(clientConfigurationClassForProvider);

        JsonObject jsonSecrets = getSecretFields(fieldsDescriptionsAndExamples);
        JsonObject jsonSensitive = getSensitiveFields(fieldsDescriptionsAndExamples);
        jsonConfigurationTemplate.add("secrets", jsonSecrets);
        jsonConfigurationTemplate.add("sensitive", jsonSensitive);

        return jsonConfigurationTemplates;
    }

    private JsonObject getSensitiveFields(Map<String, String> fieldsDescriptionsAndExamples) {
        JsonObject sensitiveFieldsJson = new JsonObject();

        Set<Field> sensitiveFieldsSet =
                clientConfigurationMetaInfoHandler.getSensitiveSecretFields();

        sensitiveFieldsSet.forEach(
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

        secretFieldsSet.forEach(
                secretField ->
                        secretFieldsJson.addProperty(
                                clientConfigurationMetaInfoHandler.fieldToFieldName.apply(
                                        secretField),
                                getDescriptionAndExample(
                                        secretField, fieldsDescriptionsAndExamples)));

        return secretFieldsJson;
    }

    private String getDescriptionAndExample(
            Field field, Map<String, String> fieldsDescriptionsAndExamples) {
        String fieldName = clientConfigurationMetaInfoHandler.fieldToFieldName.apply(field);

        StringBuilder sb = new StringBuilder();

        if (includeDescriptions) {
            String fieldDescriptionKey = fieldName + KEY_DESCRIPTION;
            if (fieldsDescriptionsAndExamples.containsKey(fieldDescriptionKey)) {
                sb.append(System.lineSeparator());
                sb.append("Description:");
                sb.append(System.lineSeparator());
                sb.append(fieldsDescriptionsAndExamples.get(fieldDescriptionKey));
            }
        }

        if (includeExamples) {
            String fieldExampleKey = fieldName + KEY_EXAMPLES;
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
