package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.util.Strings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BbvaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class BbvaConfiguration implements ClientConfiguration {

    @JsonIgnore
    private static final Logger logger = LoggerFactory.getLogger(BbvaConfiguration.class);
    @JsonProperty private String clientId;
    @JsonProperty private String clientSecret;
    @JsonProperty private String redirectUrl;
    @JsonProperty private String baseAuthUrl;
    @JsonProperty private String baseApiUrl;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getBaseAuthUrl() {
        return baseAuthUrl;
    }

    public String getBaseApiUrl() {
        return baseApiUrl;
    }

    public boolean isValid() {
        if (!Strings.isNullOrEmpty(clientId)
                && !Strings.isNullOrEmpty(clientSecret)
                && !Strings.isNullOrEmpty(redirectUrl)
                && !Strings.isNullOrEmpty(baseAuthUrl)
                && !Strings.isNullOrEmpty(baseApiUrl)) {
            return true;
        } else {
            final List<String> list = new ArrayList<>();

            if (Strings.isNullOrEmpty(clientId)) {
                list.add("clientId");
            }

            if (Strings.isNullOrEmpty(clientSecret)) {
                list.add("clientSecret");
            }

            if (Strings.isNullOrEmpty(redirectUrl)) {
                list.add("redirectUrl");
            }

            if (Strings.isNullOrEmpty(baseAuthUrl)) {
                list.add("baseAuthUrl");
            }

            if (Strings.isNullOrEmpty(baseApiUrl)) {
                list.add("baseApiUrl");
            }

            logger.error(
                    BbvaConstants.Exceptions.MISSING_CONFIGURATION_LOG,
                    BbvaConstants.LogTags.MISSING_CONFIGURATION,
                    Arrays.toString(list.toArray()));
            return false;
        }
    }
}
