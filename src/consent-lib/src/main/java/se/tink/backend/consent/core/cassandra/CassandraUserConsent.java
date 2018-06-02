package se.tink.backend.consent.core.cassandra;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;
import se.tink.backend.consent.core.Action;
import se.tink.libraries.versioning.SemanticVersion;

@Table(value = "users_consents")
public class CassandraUserConsent {
    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;

    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private UUID id;

    /**
     * The key of the consent. Example "APP_TERMS_AND_CONDITIONS" or "PEER_COMPARISON_CONSENT".
     */
    private String key;

    /**
     * The version of the consent. Example "1.0.0".
     */
    private String version;

    /**
     * The locale of the consent. Example "nl_NL".
     */
    private String locale;

    /**
     * The username that was used when the consent was given or withdrawn. Example "erik.pettersson@tink.se"
     */
    private String userName;

    /**
     * Optional payload added to the consent that the user gave.
     */
    private ByteBuffer payload;

    /**
     * Either ACCEPTED or DECLINED, depending on the action by the user.
     */
    private String action;

    /**
     * The time when the consent was given or withdrawn. Example: "442912200000"
     */
    private Date timestamp;

    /**
     * Optional signature of the data in this consent. Available if a signature was available when the consent was
     * created. Example: "JX8GBbZMPuzGKOmjpZ/p/nNZrvKtFDi9Dsn8gPP09MFtq3mkvnV7fl0hz8X53R...".
     */
    private String signature;

    public UUID getUserId() {
        return userId;
    }

    public UUID getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getVersion() {
        return version;
    }

    public String getUserName() {
        return userName;
    }

    public String getSignature() {
        return signature;
    }

    public String getAction() {
        return action;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getLocale() {
        return locale;
    }

    /**
     * Check if the the signature is valid.
     */
    boolean hasValidSignature(Signature verificationSignature) throws SignatureException {
        Preconditions.checkState(!Strings.isNullOrEmpty(this.signature));

        verificationSignature.update(getSignatureBytes());
        return verificationSignature.verify(Base64.getDecoder().decode(this.signature));
    }

    /**
     * Create the bytes that are used when the signature is created or verified.
     *
     * @return byte[] with the bytes that should be signed or verified.
     */
    private byte[] getSignatureBytes() {
        StringBuilder sb = new StringBuilder();
        sb.append(userId.toString());
        sb.append(id.toString());
        sb.append(key);
        sb.append(version);
        sb.append(locale);
        sb.append(userName);
        sb.append(action);
        sb.append(timestamp.toString());

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);

        return payload == null ? bytes : ArrayUtils.addAll(bytes, payload.array());
    }

    public static CassandraUserConsent.Builder builder() {
        return new CassandraUserConsent.Builder();
    }

    public final static class Builder {
        private UUID userId;
        private String key;
        private String version;
        private byte[] payload;
        private String locale;
        private String userName;
        private Action action;
        private Signature signature;

        public Builder withConsent(CassandraConsent cassandraConsent) {
            this.key = cassandraConsent.getKey();
            this.locale = cassandraConsent.getLocale();
            this.version = cassandraConsent.getVersion();
            return this;
        }

        public Builder withPayload(byte[] payload) {
            this.payload = payload;
            return this;
        }

        public Builder withAction(Action action) {
            this.action = action;
            return this;
        }

        public Builder withUserId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder withUsername(String username) {
            this.userName = username;
            return this;
        }

        public Builder withSignature(Signature signature) {
            this.signature = signature;
            return this;
        }

        public CassandraUserConsent build() throws SignatureException {
            // Check all inputs
            Preconditions.checkNotNull(userId, "UserId must not be null.");
            Preconditions.checkNotNull(action, "Action must not be null.");

            Preconditions.checkState(!Strings.isNullOrEmpty(key), "Key must not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(version), "Version must not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(locale), "Locale must not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(userName), "UserName must not be null or empty.");
            Preconditions.checkState(SemanticVersion.isValid(version), "Version must be a valid semantic version.");

            // Create the user consent
            CassandraUserConsent cassandraUserConsent = new CassandraUserConsent();

            cassandraUserConsent.userId = this.userId;
            cassandraUserConsent.id = UUID.randomUUID();
            cassandraUserConsent.key = this.key;
            cassandraUserConsent.version = this.version;
            cassandraUserConsent.locale = this.locale;
            cassandraUserConsent.userName = this.userName;
            cassandraUserConsent.action = this.action.toString();
            cassandraUserConsent.timestamp = new Date();
            cassandraUserConsent.payload = (this.payload == null ? null : ByteBuffer.wrap(this.payload));

            // Sign the user consent with the signature if it is provided.
            if (signature != null) {
                signature.update(cassandraUserConsent.getSignatureBytes());
                byte[] signedData = signature.sign();
                cassandraUserConsent.signature = Base64.getEncoder().encodeToString(signedData);
            }

            return cassandraUserConsent;
        }
    }
}
