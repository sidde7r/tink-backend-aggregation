package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.util.Strings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

public class CrosskeyBaseConfiguration implements ClientConfiguration {

    @JsonIgnore
    private static final Logger logger = LoggerFactory.getLogger(CrosskeyBaseConfiguration.class);

    @JsonIgnore private static final LogTag MISSING_CONFIG = LogTag.from("CROSSKEY_MISSING_CONFIG");
    @JsonProperty private String clientId;
    @JsonProperty private String clientSecret;
    @JsonProperty private String redirectUrl;
    @JsonProperty private String baseAuthUrl;
    @JsonProperty private String baseAPIUrl;
    @JsonProperty private String clientKeyStorePath;
    @JsonProperty private String clientKeyStorePassword;
    @JsonProperty private String clientSigningKeyPath;
    @JsonProperty private String clientSigningCertificatePath;
    @JsonProperty private String xFapiFinancialId;

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

    public String getBaseAPIUrl() {
        return baseAPIUrl;
    }

    public String getClientKeyStorePath() {
        return clientKeyStorePath;
    }

    public String getClientSigningKeyPath() {
        return clientSigningKeyPath;
    }

    public String getClientSigningCertificatePath() {
        return clientSigningCertificatePath;
    }

    public String getClientKeyStorePassword() {
        return clientKeyStorePassword;
    }

    public String getXFapiFinancialId() {
        return xFapiFinancialId;
    }

    public boolean isValid() {
        if (!Strings.isNullOrEmpty(clientId)
                && !Strings.isNullOrEmpty(clientSecret)
                && !Strings.isNullOrEmpty(redirectUrl)
                && !Strings.isNullOrEmpty(baseAuthUrl)
                && !Strings.isNullOrEmpty(baseAPIUrl)
                && !Strings.isNullOrEmpty(clientKeyStorePath)
                && !Strings.isNullOrEmpty(clientKeyStorePassword)
                && !Strings.isNullOrEmpty(xFapiFinancialId)) {
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

            if (Strings.isNullOrEmpty(clientKeyStorePath)) {
                list.add("clientKeyStorePath");
            }

            if (Strings.isNullOrEmpty(clientKeyStorePassword)) {
                list.add("clientKeyStorePassword");
            }

            if (Strings.isNullOrEmpty(xFapiFinancialId)) {
                list.add("xFapiFinancialId");
            }

            logger.error(
                    CrosskeyBaseConstants.Exceptions.MISSING_CONFIGURATION_LOG,
                    MISSING_CONFIG,
                    Arrays.toString(list.toArray()));
            return false;
        }
    }
}
