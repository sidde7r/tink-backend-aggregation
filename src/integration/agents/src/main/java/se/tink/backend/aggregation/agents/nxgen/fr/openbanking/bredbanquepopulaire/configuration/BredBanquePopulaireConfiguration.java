package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;

@JsonObject
@Getter
public class BredBanquePopulaireConfiguration implements ClientConfiguration {

    @JsonProperty @Secret @ClientIdConfiguration private String clientId;

    @JsonSchemaTitle("Subscription key")
    @JsonSchemaDescription("Key obtained on developer portal after subscription of the API")
    @JsonProperty
    @SensitiveSecret
    private String ocpApimSubscriptionKey;

    @JsonSchemaTitle("Link to public QSeal certificate")
    @JsonSchemaDescription(
            "Certificate used to register the app without new lines, header and footer")
    @JsonProperty
    @Secret
    private String keyId;
}
