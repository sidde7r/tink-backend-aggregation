package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.configuration;

import com.google.common.base.Preconditions;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.ClientConfiguration;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
public class NordeaPartnerConfiguration implements ClientConfiguration {
    @Secret private String partnerId;
    @Secret private String baseUrl;
    @Secret private String nordeaSigningPublicKey;
    @Secret private String nordeaEncryptionPublicKey;
    @SensitiveSecret private String tinkSingingPrivateKey;
    @Secret private String tinkSingingPublicKey;
    @SensitiveSecret private String tinkEncryptionPrivateKey;
    @Secret private String tinkEncryptionPublicKey;
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

    public String getNordeaSigningPublicKey() {
        Preconditions.checkState(
                !Strings.isNullOrEmpty(nordeaSigningPublicKey),
                "Invalid configuration - nordeaSigningPublicKey shouldn't be empty/null");
        return nordeaSigningPublicKey;
    }

    public String getNordeaEncryptionPublicKey() {
        Preconditions.checkState(
                !Strings.isNullOrEmpty(nordeaEncryptionPublicKey),
                "Invalid configuration - nordeaEncryptionPublicKey shouldn't be empty/null");
        return nordeaEncryptionPublicKey;
    }

    public String getTinkSingingPrivateKey() {
        Preconditions.checkState(
                !Strings.isNullOrEmpty(tinkSingingPrivateKey),
                "Invalid configuration - tinkSingingPrivateKey shouldn't be empty/null");
        return tinkSingingPrivateKey;
    }

    public String getTinkSingingPublicKey() {
        Preconditions.checkState(
                !Strings.isNullOrEmpty(tinkSingingPublicKey),
                "Invalid configuration - tinkSingingPublicKey shouldn't be empty/null");
        return tinkSingingPublicKey;
    }

    public String getTinkEncryptionPrivateKey() {
        Preconditions.checkState(
                !Strings.isNullOrEmpty(tinkEncryptionPrivateKey),
                "Invalid configuration - tinkEncryptionPrivateKey shouldn't be empty/null");
        return tinkEncryptionPrivateKey;
    }

    public String getTinkEncryptionPublicKey() {
        Preconditions.checkState(
                !Strings.isNullOrEmpty(tinkEncryptionPublicKey),
                "Invalid configuration - tinkEncryptionPublicKey shouldn't be empty/null");
        return tinkEncryptionPublicKey;
    }

    public String getKeyId() {
        Preconditions.checkState(
                !Strings.isNullOrEmpty(keyId),
                "Invalid configuration - keyId shouldn't be empty/null");
        return keyId;
    }
}
