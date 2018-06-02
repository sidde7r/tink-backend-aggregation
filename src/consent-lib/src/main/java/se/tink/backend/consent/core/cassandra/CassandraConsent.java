package se.tink.backend.consent.core.cassandra;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.google.common.collect.Maps;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.versioning.SemanticVersion;

@Table(value = "consents")
public class CassandraConsent {
    /**
     * The key of the consent. Example "APP_TERMS_AND_CONDITIONS" or "PEER_COMPARISON_CONSENT".
     */
    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String key;

    /**
     * The semantic version of the consent. Example "1.0.0".
     */
    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private String version;

    /**
     * The locale of the consent. Example "nl_NL".
     */
    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private String locale;

    /**
     * The title of the consent. Example "Terms & Conditions"
     */
    private String title;

    /**
     * The body of the consent. Can be any format (i.e. plaintext/markup/json). Example "<html>....</html>".
     */
    private String body;

    /**
     * Any optional messages that are connected to the consent. Example "I accept the T&C stated..."
     */
    private List<String> messages;

    /**
     * Any optional attachments that are connected to the consent. Can be any format (i.e. plaintext/markup/json).
     * Each attachment is defined by a key. Example "PRIVACY_POLICY: <html>....</html>".
     */
    private Map<String, String> attachments;

    public String getKey() {
        return key;
    }

    public String getVersion() {
        return version;
    }

    public String getLocale() {
        return locale;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public List<String> getMessages() {
        return messages;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    /**
     * Calculate a checksum of this consent. Useful to be able to see that nothing has changed in the backend/database
     * between the time period when a user requested the available consents and when the consent was accepted.
     */
    public String getChecksum() {
        StringBuilder content = new StringBuilder();
        content.append(key);
        content.append(version);
        content.append(locale);
        content.append(title);

        if (body != null) {
            content.append(body);
        }

        if (messages != null) {
            messages.forEach(content::append);
        }

        if (attachments != null) {
            attachments.forEach((a, b) -> content.append(a).append(b));
        }

        return Hashing.sha512().hashString(content.toString(), StandardCharsets.UTF_8).toString();
    }

    public boolean isUpdateFor(CassandraConsent input) {
        if (!Objects.equals(this.key, input.getKey()) || !Objects.equals(this.locale, input.getLocale())) {
            return false;
        }

        SemanticVersion thisVersion;
        SemanticVersion inputVersion;

        try {
            thisVersion = new SemanticVersion(version);
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("Could not parse = '%s')", version), e);
        }

        try {
            inputVersion = new SemanticVersion(input.getVersion());
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("Could not parse = '%s')", input.getVersion()), e);
        }

        return thisVersion.isUpdateFor(inputVersion);
    }

    public static CassandraConsent.Builder builder() {
        return new CassandraConsent.Builder();
    }

    public final static class Builder {
        private String key;
        private String version;
        private String locale;
        private String title;
        private String body;
        private List<String> messages = Lists.newArrayList();
        private Map<String, String> attachments = Maps.newHashMap();

        public Builder withKey(String key) {
            this.key = key;
            return this;
        }

        public Builder withVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder withLocale(String locale) {
            this.locale = locale;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withAttachment(String key, String content) {
            this.attachments.put(key, content);
            return this;
        }

        public Builder withBody(String body) {
            this.body = body;
            return this;
        }

        public Builder withMessage(MessageEntity message) {
            this.messages.add(SerializationUtils.serializeToString(message));
            return this;
        }

        public CassandraConsent build() {
            Preconditions.checkState(!Strings.isNullOrEmpty(key), "Key must not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(version), "Version must not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(locale), "Locale must not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(title), "Title must not be null or empty.");
            Preconditions.checkState(SemanticVersion.isValid(version), "Version must be a valid semantic version.");

            CassandraConsent configuration = new CassandraConsent();
            configuration.key = key;
            configuration.version = version;
            configuration.locale = locale;
            configuration.title = title;
            configuration.body = body;
            configuration.attachments = attachments;
            configuration.messages = messages;

            return configuration;
        }
    }
}
