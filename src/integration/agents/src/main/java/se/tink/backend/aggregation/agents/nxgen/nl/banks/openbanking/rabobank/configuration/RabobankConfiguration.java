package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@JsonObject
public class RabobankConfiguration implements ClientConfiguration {
    @JsonProperty private String clientId;
    @JsonProperty private String clientSecret;
    @JsonProperty private String clientSSLKeyPassword;
    @JsonProperty private String clientSSLP12;
    @JsonProperty private String redirectUrl;

    @JsonIgnore
    private static final Logger logger = LoggerFactory.getLogger(RabobankConfiguration.class);

    @JsonIgnore private static final LogTag MISSING_CONFIG = LogTag.from("RABOBANK_MISSING_CONFIG");

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getClientSSLKeyPassword() {
        return clientSSLKeyPassword;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getClientSSLP12() {
        return clientSSLP12;
    }

    public boolean isValid() {
        if (!Strings.isNullOrEmpty(clientId)
                && !Strings.isNullOrEmpty(clientSecret)
                && !Strings.isNullOrEmpty(clientSSLP12)
                && !Objects.isNull(clientSSLKeyPassword)
                && !Strings.isNullOrEmpty(redirectUrl)) {
            return true;
        } else {
            final List<String> list = new ArrayList<>();

            if (Strings.isNullOrEmpty(clientId)) {
                list.add("clientId");
            }

            if (Strings.isNullOrEmpty(clientSecret)) {
                list.add("clientSecret");
            }

            if (Strings.isNullOrEmpty(clientSSLP12)) {
                list.add("clientSSLP12");
            }

            if (Objects.isNull(clientSSLKeyPassword)) {
                list.add("clientSSLKeyPassword");
            }

            if (Strings.isNullOrEmpty(redirectUrl)) {
                list.add("redirectUrl");
            }

            logger.error(
                    "{} - Missing Rabobank configuration: {}",
                    MISSING_CONFIG,
                    Arrays.toString(list.toArray()));
            return false;
        }
    }
}
