package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.configuration;

import com.google.common.base.Preconditions;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class NordeaPartnerConfiguration implements ClientConfiguration {
    private String partnerId;
    private String baseUrl;
    private String nordeaSigningPublicKey;
    private String nordeaEncryptionPublicKey;
    private String tinkSingingPrivateKey;
    private String tinkSingingPublicKey;
    private String tinkEncryptionPrivateKey;
    private String tinkEncryptionPublicKey;
    private String kid;

    public String getBaseUrl() {
        Preconditions.checkState(
                !Strings.isNullOrEmpty(baseUrl),
                "Invalid configuration - baseUrl shouldn't be empty/null");
        return baseUrl;
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

    public String getKid() {
        Preconditions.checkState(
                !Strings.isNullOrEmpty(kid), "Invalid configuration - kid shouldn't be empty/null");
        return kid;
    }
}
