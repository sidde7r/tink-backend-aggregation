package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.configuration.BerlinGroupConfiguration;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LaBanquePostaleConfiguration extends BerlinGroupConfiguration {
    @JsonProperty private String oauthBaseUrl;

    public String getOauthBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(oauthBaseUrl),
                String.format(
                        LaBanquePostaleConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "oauth base url"));

        return oauthBaseUrl;
    }
}
