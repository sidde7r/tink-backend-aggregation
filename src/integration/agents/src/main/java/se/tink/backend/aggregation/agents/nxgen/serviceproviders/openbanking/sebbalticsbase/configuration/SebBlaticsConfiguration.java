package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsCommonConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

public class SebBlaticsConfiguration implements ClientConfiguration {

    @Secret
    @JsonProperty(required = true)
    @JsonSchemaDescription(
            "ClientId received from SEB's onboarding API. Usually your PSD2 authorization number followed by 'p' for Private, or 'c' for Corporate.")
    @JsonSchemaTitle("Client ID")
    @JsonSchemaExamples("PSDXX-XYZ-123456p")
    private String clientId;

    private String bic;

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    @JsonProperty(required = true)
    public String getBic() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(bic),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "bic"));

        return bic;
    }
}
