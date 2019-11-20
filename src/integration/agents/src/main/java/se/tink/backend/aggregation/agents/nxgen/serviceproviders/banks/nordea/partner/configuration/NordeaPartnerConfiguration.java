package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.configuration;

import com.google.common.base.Preconditions;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
public class NordeaPartnerConfiguration implements ClientConfiguration {
    @Secret private String partnerId;
    @Secret private String baseUrl;
    @Secret private String nordeaEncryptionPublicKey;
    @SensitiveSecret private String partnerKeystorePassword;
    @Secret private String keyId;

    public URL getBaseUrl() {
        Preconditions.checkState(
                !Strings.isNullOrEmpty(baseUrl),
                "Invalid configuration - baseUrl shouldn't be empty/null");
        return new URL(baseUrl);
    }

    public String getPartnerId() {
        Preconditions.checkState(
                !Strings.isNullOrEmpty(partnerId),
                "Invalid configuration - partnerId shouldn't be empty/null");
        return partnerId;
    }

    public String getNordeaEncryptionPublicKey() {
        Preconditions.checkState(
                !Strings.isNullOrEmpty(nordeaEncryptionPublicKey),
                "Invalid configuration - nordeaEncryptionPublicKey shouldn't be empty/null");
        return nordeaEncryptionPublicKey;
    }

    public String getKeyId() {
        Preconditions.checkState(
                !Strings.isNullOrEmpty(keyId),
                "Invalid configuration - keyId shouldn't be empty/null");
        return keyId;
    }

    public String getPartnerKeystorePassword() {
        Preconditions.checkState(
                !Strings.isNullOrEmpty(partnerKeystorePassword),
                "Invalid configuration - partnerKeystorePassword shouldn't be empty/null");
        return partnerKeystorePassword;
    }
}
