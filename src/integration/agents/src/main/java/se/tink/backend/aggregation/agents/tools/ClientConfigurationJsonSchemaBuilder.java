package se.tink.backend.aggregation.agents.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.lang3.StringEscapeUtils;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.annotations.Views;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

public class ClientConfigurationJsonSchemaBuilder {
    private static final int NUM_SPACES_INDENT = 10;
    private static final String PRETTY_PRINTING_INDENT_PADDING =
            new String(new char[NUM_SPACES_INDENT]).replace((char) 0, ' ');
    private static final String KEY_SECRETS = "secrets";
    private static final String KEY_ENCRYPTED_SECRETS = "encryptedSecrets";
    private static final String KEY_PROPERTIES = "properties";
    private static final String PROVIDER_NAME_KEY = "providerId";

    private final String providerName;
    private final ClientConfigurationMetaInfoHandler clientConfigurationMetaInfoHandler;

    private Set<String> secretsFieldName = new HashSet<>();
    private Set<String> encryptedSecretsFieldName = new HashSet<>();

    public ClientConfigurationJsonSchemaBuilder(Provider provider) {
        this.providerName = provider.getName();
        this.clientConfigurationMetaInfoHandler = new ClientConfigurationMetaInfoHandler(provider);
        Preconditions.checkNotNull(
                Strings.emptyToNull(this.providerName),
                "providerName in requested provider cannot be null.");
        Set<Field> secrets = clientConfigurationMetaInfoHandler.getSecretFields();
        for (Field field : secrets) {
            secretsFieldName.add(clientConfigurationMetaInfoHandler.fieldToFieldName.apply(field));
        }
        Set<Field> encryptedSecrets = clientConfigurationMetaInfoHandler.getSensitiveSecretFields();
        for (Field field : encryptedSecrets) {
            encryptedSecretsFieldName.add(
                    clientConfigurationMetaInfoHandler.fieldToFieldName.apply(field));
        }
    }

    public String buildJsonSchema() {
        Class<? extends ClientConfiguration> clientConfigurationClassForProvider =
                clientConfigurationMetaInfoHandler.findClosestClientConfigurationClass();

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        mapper.setConfig(mapper.getSerializationConfig().withView(Views.Public.class));
        try {
            JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(mapper);
            JsonNode flatSchemaFromConf =
                    jsonSchemaGenerator.generateJsonSchema(clientConfigurationClassForProvider);

            Iterator<String> fieldNames = flatSchemaFromConf.fieldNames();
            ObjectNode finalNode = mapper.createObjectNode();
            finalNode.put(PROVIDER_NAME_KEY, this.providerName);

            JsonNode propertiesNode = null;

            // put fields other than properties to finalNode
            while (fieldNames.hasNext()) {
                String field = fieldNames.next();
                JsonNode fieldValue = flatSchemaFromConf.get(field);
                if (!KEY_PROPERTIES.equals(field)) {
                    finalNode.set(field, fieldValue);
                } else {
                    propertiesNode = fieldValue;
                }
            }
            constructSecretsSchema(propertiesNode, finalNode);

            return replaceUnwantedCharacters(mapper.writeValueAsString(finalNode));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Json schema mapping failed", e);
        }
    }

    private void constructSecretsSchema(JsonNode root, ObjectNode finalNode) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode secretsPropertiesNode = mapper.createObjectNode();
        ObjectNode encryptedSecretsPropertiesNode = mapper.createObjectNode();
        ObjectNode finalPropertiesNode = mapper.createObjectNode();
        ObjectNode secretsNode = initWithTypeObject(mapper);
        ObjectNode encryptedSecretsNode = initWithTypeObject(mapper);

        Iterator<String> fieldNames = root.fieldNames();

        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode fieldValue = root.get(fieldName);

            if (secretsFieldName.contains(fieldName)) {
                secretsPropertiesNode.set(fieldName, fieldValue);
            } else if (encryptedSecretsFieldName.contains(fieldName)) {
                encryptedSecretsPropertiesNode.set(fieldName, fieldValue);
            }
        }

        secretsNode.set(KEY_PROPERTIES, secretsPropertiesNode);
        encryptedSecretsNode.set(KEY_PROPERTIES, encryptedSecretsPropertiesNode);

        finalPropertiesNode.set(KEY_SECRETS, secretsNode);
        finalPropertiesNode.set(KEY_ENCRYPTED_SECRETS, encryptedSecretsNode);

        finalNode.set(KEY_PROPERTIES, finalPropertiesNode);
    }

    private ObjectNode initWithTypeObject(ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "object");
        return node;
    }

    private String replaceUnwantedCharacters(String jsonString) {
        return StringEscapeUtils.unescapeJava(
                jsonString.replace("\\n", "\n" + PRETTY_PRINTING_INDENT_PADDING));
    }
}
