package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
public class ICSConfiguration implements ClientConfiguration {
    @JsonProperty @Secret @ClientIdConfiguration private String clientId;
    @JsonProperty @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;

    @JsonIgnore
    private static final Logger logger = LoggerFactory.getLogger(ICSConfiguration.class);

    @JsonIgnore private static final LogTag MISSING_CONFIG = LogTag.from("ICS_MISSING_CONFIG");

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public boolean isValid() {
        if (!Strings.isNullOrEmpty(clientId) && !Strings.isNullOrEmpty(clientSecret)) {
            return true;
        } else {
            List<String> list = new ArrayList<>();

            if (Strings.isNullOrEmpty(clientId)) {
                list.add("clientId");
            }

            if (Strings.isNullOrEmpty(clientSecret)) {
                list.add("clientSecret");
            }

            logger.error(
                    "{} - Missing ICS configuration: {}",
                    MISSING_CONFIG,
                    Arrays.toString(list.toArray()));

            return false;
        }
    }
}
