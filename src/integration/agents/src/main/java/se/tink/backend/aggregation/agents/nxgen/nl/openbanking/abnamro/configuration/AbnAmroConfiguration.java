package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaString;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import java.util.Set;
import lombok.Getter;
import se.tink.backend.aggregation.agents.consent.generators.nlob.abnamro.AbnAmroScope;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;

@JsonObject
@Getter
public class AbnAmroConfiguration implements ClientConfiguration {
    @JsonProperty @ClientIdConfiguration @Secret private String clientId;

    @JsonProperty
    @JsonSchemaTitle("Api Key")
    @JsonSchemaDescription("Api Key is a public identifier for apps.")
    @JsonSchemaExamples("vhjoYm9Bo4UciG8Xh8W0F4n05o3XpaET")
    @JsonSchemaInject(strings = {@JsonSchemaString(path = "pattern", value = "^[A-Za-z0-9]{32}$")})
    @Secret
    private String apiKey;

    public static Set<AbnAmroScope> getAbnAmroScopes() {
        return Sets.newHashSet(
                AbnAmroScope.READ_ACCOUNTS,
                AbnAmroScope.READ_ACCOUNTS_DETAILS,
                AbnAmroScope.READ_TRANSACTIONS_HISTORY);
    }
}
