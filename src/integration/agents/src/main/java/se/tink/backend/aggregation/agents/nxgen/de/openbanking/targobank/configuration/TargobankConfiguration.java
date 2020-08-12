package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.TargobankConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class TargobankConfiguration implements ClientConfiguration {

    @JsonSchemaTitle("API key")
    @JsonSchemaExamples("tUfkQln5LvdeeduevPM3qrCG7Y8J8CbF")
    @JsonSchemaDescription("API key needed to access bank endpoints")
    @SensitiveSecret
    private String apiKey;

    public String getApiKey() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(apiKey),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "API Key"));

        return apiKey;
    }
}
