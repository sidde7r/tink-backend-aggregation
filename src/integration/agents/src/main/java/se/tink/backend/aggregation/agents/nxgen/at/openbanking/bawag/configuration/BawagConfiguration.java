package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.BawagConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class BawagConfiguration implements ClientConfiguration {

    private String keystorePath;
    private String keystorePassword;

    public String getKeystorePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(keystorePath),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Keystore path"));

        return keystorePath;
    }

    public String getKeystorePassword() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(keystorePassword),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Keystore password"));

        return keystorePassword;
    }
}
