package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaString;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;
import se.tink.backend.aggregation.configuration.agents.QSealCConfiguration;

@JsonObject
public class SwedbankConfiguration implements ClientConfiguration {

    @Secret @ClientIdConfiguration private String clientId;
    @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;
    @Secret @QSealCConfiguration private String qSealc;

    @Secret
    @JsonProperty(required = true)
    @JsonSchemaDescription(
            "Key ID, extracted from QSealC, format is like: SN=[Signing certificate serial number],CA=base64([Signing certificate issuer DN name ])")
    @JsonSchemaTitle("Key ID")
    @JsonSchemaInject(strings = {@JsonSchemaString(path = "pattern", value = "^SN=.+,CA=.+$")})
    @JsonSchemaExamples(
            "SN=a123456ca87dc6204fedba3223e59c09,CA=Q049RFVNTVkgUVRTUCwgT1U9Q2VydGlmaWNhdGlvbiBBdXRob3JpdHksIE89UVRTUCBPUkcgTkFNRSAsIEM9U0U=")
    private String keyIdBase64;

    public String getQSealc() {
        Preconditions.checkNotNull(
                com.google.common.base.Strings.emptyToNull(qSealc),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "qSealc"));

        return qSealc;
    }

    public String getKeyIdBase64() {
        Preconditions.checkNotNull(
                com.google.common.base.Strings.emptyToNull(keyIdBase64),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "KeyId"));

        return keyIdBase64;
    }

    public String getClientId() {
        Preconditions.checkNotNull(
                com.google.common.base.Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                com.google.common.base.Strings.emptyToNull(clientSecret),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Secret"));

        return clientSecret;
    }
}
