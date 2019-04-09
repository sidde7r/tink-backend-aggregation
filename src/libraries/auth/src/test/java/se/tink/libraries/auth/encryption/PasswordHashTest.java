package se.tink.libraries.auth.encryption;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Test;

public class PasswordHashTest {
    @Test
    public void testHashPassword_correctPrefixForScrypt() {
        String hash = PasswordHash.create("tink", HashingAlgorithm.SCRYPT);
        assertThat(hash.startsWith("$s0$")).isTrue();
    }

    @Test
    public void testHashPassword_correctPrefixForBcrypt() {
        String hash = PasswordHash.create("tink", HashingAlgorithm.BCRYPT);
        assertThat(hash.startsWith("$2a$")).isTrue();
    }

    @Test
    public void testCheckPassword_shortFormat() {
        String secureHashed = "$2a$06$1/8RIMC9j6nTAh5QHBHSgerldAwsHIy8EFEJ3rA9UOsdcgKSV3euS";
        assertThat(PasswordHash.check("secure", secureHashed, HashingAlgorithm.BCRYPT)).isTrue();
        assertThat(PasswordHash.check("", secureHashed, HashingAlgorithm.BCRYPT)).isFalse();
        assertThat(PasswordHash.check(secureHashed, secureHashed, HashingAlgorithm.BCRYPT))
                .isFalse();
        assertThat(PasswordHash.check("tink", secureHashed, HashingAlgorithm.BCRYPT)).isFalse();
        assertThat(PasswordHash.check("secure", secureHashed, HashingAlgorithm.SCRYPT)).isFalse();
    }

    @Test
    public void testCheckPassword_authenticateScryptPassword() {
        List<HashingAlgorithm> allowedHashsWithScrypt =
                Lists.newArrayList(HashingAlgorithm.SCRYPT, HashingAlgorithm.BCRYPT);
        List<HashingAlgorithm> allowedHashsNoScrypt = Lists.newArrayList(HashingAlgorithm.BCRYPT);

        String tinkHashed =
                "$s0$e0801$QZCpKu9IyySUgjvEU2FLwQ==$HUb1iHi6eDsDAMN0LPaLifAzpbwbhp4LdXz/IFUkTBI=";
        assertThat(PasswordHash.check(tinkHashed, tinkHashed, allowedHashsWithScrypt)).isFalse();
        assertThat(PasswordHash.check("foo", tinkHashed, allowedHashsWithScrypt)).isFalse();
        assertThat(PasswordHash.check("tink", tinkHashed, allowedHashsWithScrypt)).isTrue();
        assertThat(PasswordHash.check("tink", tinkHashed, allowedHashsNoScrypt)).isFalse();
    }
}
