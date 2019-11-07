package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.configuration;

import com.google.common.base.Preconditions;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
public class NordeaPartnerConfiguration implements ClientConfiguration {
    @AgentConfigParam private String redirectUrl;
    @Secret private String partnerId;
    @Secret private String baseUrl;
    @Secret private String nordeaSigningPublicKey;
    @Secret private String nordeaEncryptionPublicKey;
    @Secret private String tinkSingingPrivateKey;
    @SensitiveSecret private String tinkSingingKeyPassword;
    @Secret private String tinkEncryptionPrivateKey;
    @SensitiveSecret private String tinkEncryptionKeyPassword;
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

    public String getTinkEncryptionPrivateKey() {
        Preconditions.checkState(
                !Strings.isNullOrEmpty(tinkEncryptionPrivateKey),
                "Invalid configuration - tinkEncryptionPrivateKey shouldn't be empty/null");
        return tinkEncryptionPrivateKey;
    }

    public String getKeyId() {
        Preconditions.checkState(
                !Strings.isNullOrEmpty(keyId),
                "Invalid configuration - keyId shouldn't be empty/null");
        return keyId;
    }

    public String getTinkSingingKeyPassword() {
        Preconditions.checkState(
                !Strings.isNullOrEmpty(tinkSingingKeyPassword),
                "Invalid configuration - tinkSingingKeyPassword shouldn't be empty/null");
        return tinkSingingKeyPassword;
    }

    public String getTinkEncryptionKeyPassword() {
        Preconditions.checkState(
                !Strings.isNullOrEmpty(tinkEncryptionKeyPassword),
                "Invalid configuration - tinkEncryptionKeyPassword shouldn't be empty/null");
        return tinkEncryptionKeyPassword;
    }
}
