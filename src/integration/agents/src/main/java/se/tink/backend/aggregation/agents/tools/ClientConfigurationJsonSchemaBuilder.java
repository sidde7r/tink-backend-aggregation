package se.tink.backend.aggregation.agents.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringEscapeUtils;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

public class ClientConfigurationJsonSchemaBuilder {
    private static final int NUM_SPACES_INDENT = 10;
    private static final String PRETTY_PRINTING_INDENT_PADDING =
            new String(new char[NUM_SPACES_INDENT]).replace((char) 0, ' ');
    private static final String KEY_FIN_ID = "financialInstitutionId";
    private static final String KEY_SECRETS = "secrets";
    private static final String KEY_ENCRYPTED_SECRETS = "encryptedSecrets";

    private final String financialInstitutionId;
    private final ClientConfigurationMetaInfoHandler clientConfigurationMetaInfoHandler;

    public ClientConfigurationJsonSchemaBuilder(Provider provider) {
        this.financialInstitutionId = provider.getFinancialInstitutionId();
        this.clientConfigurationMetaInfoHandler = new ClientConfigurationMetaInfoHandler(provider);
        Preconditions.checkNotNull(
                Strings.emptyToNull(this.financialInstitutionId),
                "financialInstitutionId in requested provider cannot be null.");
    }

    public String buildJsonSchema() {
        Class<? extends ClientConfiguration> clientConfigurationClassForProvider =
                clientConfigurationMetaInfoHandler.findClosestClientConfigurationClass();

        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);

        try {
            JsonSchema schema = schemaGen.generateSchema(clientConfigurationClassForProvider);
            StringSchema finIdSchema = new StringSchema();
            finIdSchema.setPattern(this.financialInstitutionId);

            Set<Field> secretFieldsSet = clientConfigurationMetaInfoHandler.getSecretFields();
            ObjectSchema secretSchema = processSecretsSchemaBlock(secretFieldsSet);

            Set<Field> encryptedSecretFieldsSet =
                    clientConfigurationMetaInfoHandler.getSensitiveSecretFields();
            ObjectSchema encryptedSecretSchema =
                    processSecretsSchemaBlock(encryptedSecretFieldsSet);

            final ObjectSchema finalSchema = new ObjectSchema();
            finalSchema.putProperty(KEY_FIN_ID, finIdSchema);
            finalSchema.putProperty(KEY_SECRETS, secretSchema);
            finalSchema.putProperty(KEY_ENCRYPTED_SECRETS, encryptedSecretSchema);
            finalSchema.setId(schema.getId());
            finalSchema.setDescription(
                    String.format(
                            "This is the json schema for TPP credential of financial institution %s",
                            schema.getId()));
            finalSchema.setTitle("TPP Credential");

            String jsonSchemaResult =
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(finalSchema);

            return replaceUnwantedCharacters(jsonSchemaResult);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Json schema mapping failed", e);
        }
    }

    private ObjectSchema processSecretsSchemaBlock(Set<Field> fieldsSet) {

        final Map<String, JsonSchema> secretsProperties = new HashMap<>();
        // TODO add more logic in defining schema
        for (Field field : fieldsSet) {
            if (field.getType() == String.class) {
                StringSchema secrets = new StringSchema();
                secrets.setDescription(field.getName());
                secretsProperties.put(field.getName(), secrets);
            }
        }
        final ObjectSchema objectSchema = new ObjectSchema();
        objectSchema.setProperties(secretsProperties);
        return objectSchema;
    }

    private String replaceUnwantedCharacters(String jsonString) {
        return StringEscapeUtils.unescapeJava(
                jsonString.replace("\\n", "\n" + PRETTY_PRINTING_INDENT_PADDING));
    }
}
