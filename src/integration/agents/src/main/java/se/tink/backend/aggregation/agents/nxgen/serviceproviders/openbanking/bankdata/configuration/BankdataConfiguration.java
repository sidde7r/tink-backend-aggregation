package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class BankdataConfiguration implements ClientConfiguration {

    @JsonProperty(required = true)
    @SensitiveSecret
    @JsonSchemaTitle("API Key")
    @JsonSchemaDescription(
            "Unique key generated during TPP registration on Bankdata's Developer Portal.")
    @JsonSchemaExamples("abcdefghijklmnopqrstuvwxyzabcdef")
    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }
}
