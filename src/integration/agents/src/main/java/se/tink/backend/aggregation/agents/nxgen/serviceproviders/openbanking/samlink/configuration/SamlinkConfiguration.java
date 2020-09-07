package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.configuration.BerlinGroupConfiguration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

@JsonObject
public class SamlinkConfiguration implements BerlinGroupConfiguration {

    @JsonProperty(required = true)
    @SensitiveSecret
    @JsonSchemaTitle("API key")
    @JsonSchemaDescription("Unique key generated during TPP registration to Samlink.")
    @JsonSchemaExamples("26e94058-cb98-4f5c-ba18-869d7b2f761a")
    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }

    @Override
    public String getClientId() {
        throw new NotImplementedException(
                "Value is derived from Tink Qseal certificate as organization identifier.");
    }

    @Override
    public String getClientSecret() {
        throw new NotImplementedException("Value is not a part of Samlink configuration.");
    }

    @Override
    public String getBaseUrl() {
        throw new NotImplementedException("Value is present in agents constants.");
    }

    @Override
    public String getPsuIpAddress() {
        throw new NotImplementedException("Value is not a part of Samlink configuration.");
    }
}
