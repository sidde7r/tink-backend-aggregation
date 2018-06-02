package se.tink.backend.auth;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import se.tink.libraries.auth.encryption.HashingAlgorithm;
import se.tink.libraries.auth.encryption.PasswordHash;

public class BasicAuthenticationDetails {

    private static final Joiner COLON_JOINER = Joiner.on(":");
    private static final Splitter COLON_SPLITTER = Splitter.on(":");

    private final String password;
    private final String username;
    private String hashedPassword = null;

    public BasicAuthenticationDetails(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public BasicAuthenticationDetails(String base64EncodedHeader) {
        Preconditions.checkNotNull(base64EncodedHeader);

        List<String> decodedCredentials = Lists.newArrayList(COLON_SPLITTER.split(
                new String(Base64.decodeBase64(base64EncodedHeader), Charsets.ISO_8859_1)));

        Preconditions.checkArgument(decodedCredentials.size() >= 2);

        final String username = decodedCredentials.get(0);

        // If a password contains ":" we need to join the remaining list parts together.
        decodedCredentials.remove(0);
        final String password = COLON_JOINER.join(decodedCredentials);

        this.username = username;
        this.password = password;
    }

    private String hash(String string, HashingAlgorithm algorithm) {
        if (Strings.isNullOrEmpty(string)) {
            return null;
        }
        return PasswordHash.create(string, algorithm);
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword(HashingAlgorithm algorithm) {
        if (hashedPassword == null) {
            hashedPassword = hash(password, algorithm);
        }
        return hashedPassword;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(this.getClass())) {
            return false;
        }
        final BasicAuthenticationDetails other = (BasicAuthenticationDetails) obj;
        return Objects.equal(other.password, this.password) && Objects.equal(other.username, this.username);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.username, this.password);
    }
}
