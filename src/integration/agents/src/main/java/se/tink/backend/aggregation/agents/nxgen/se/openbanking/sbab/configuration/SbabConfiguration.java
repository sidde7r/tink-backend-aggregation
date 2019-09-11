package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class SbabConfiguration implements ClientConfiguration {

    private String redirectUrl;
    private String eidasCertId;

    public String getEidasCertId() {
        return eidasCertId;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Redirect URL"));
        return redirectUrl;
    }
}
