package se.tink.libraries.cryptography;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ECDSAUtilsTest {
    private static final String PRIVATE_KEY = "-----BEGIN EC PRIVATE KEY-----\n"
            + "MIHbAgEBBEEVanejzVeLAKaLaXUZCCWWf63jHOZE1rps1q3fFvLj4RMSzX+g1fTB\n"
            + "fxXpMNbxN5vcvwOs7Drzufq9R5CzIz62IqAHBgUrgQQAI6GBiQOBhgAEAfgUTs9A\n"
            + "0DBte5Yk3OcoTb4IdbCXSDHWYtP9ZPGsdN3ImbpgVNAQ6jNP9TAFMq3pBXVxvi2d\n"
            + "KTPO64WDa3ES7tlkASa0uhC6/13STPn7cvu8rzZmeLKevnVzyMIvuOeM77o2LscW\n"
            + "jI+h8hn6KyVLrM4DgBDzj5dQGv93Np3AO5WTcZBZ\n"
            + "-----END EC PRIVATE KEY-----\n";
    private static final String PKCS8_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n"
            + "MIHtAgEAMBAGByqGSM49AgEGBSuBBAAjBIHVMIHSAgEBBEEVanejzVeLAKaLaXUZ\n"
            + "CCWWf63jHOZE1rps1q3fFvLj4RMSzX+g1fTBfxXpMNbxN5vcvwOs7Drzufq9R5Cz\n"
            + "Iz62IqGBiQOBhgAEAfgUTs9A0DBte5Yk3OcoTb4IdbCXSDHWYtP9ZPGsdN3Imbpg\n"
            + "VNAQ6jNP9TAFMq3pBXVxvi2dKTPO64WDa3ES7tlkASa0uhC6/13STPn7cvu8rzZm\n"
            + "eLKevnVzyMIvuOeM77o2LscWjI+h8hn6KyVLrM4DgBDzj5dQGv93Np3AO5WTcZBZ\n"
            + "-----END PRIVATE KEY-----\n";
    private static final String PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n"
            + "MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQB+BROz0DQMG17liTc5yhNvgh1sJdI\n"
            + "MdZi0/1k8ax03ciZumBU0BDqM0/1MAUyrekFdXG+LZ0pM87rhYNrcRLu2WQBJrS6\n"
            + "ELr/XdJM+fty+7yvNmZ4sp6+dXPIwi+454zvujYuxxaMj6HyGforJUuszgOAEPOP\n"
            + "l1Aa/3c2ncA7lZNxkFk=\n"
            + "-----END PUBLIC KEY-----\n";
    private static final String WRONG_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n"
            + "MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQBZ7z+8YoaUzUvWX5PNpfTbPd9JQWB\n"
            + "jENG8pjmW+86sQTBxE+PjjHkPDzgozoyofegq1VaIz7oy8CE1JFrv0JJm3QBdLO8\n"
            + "513N41HAhC3HPK01BedQov56BV4ELFlUIc3O4fTgRSD19hb3UyWoOr6RQ7ZFrZ0n\n"
            + "jBA0ZdHVL+70hioQbSw=\n"
            + "-----END PUBLIC KEY-----\n";
    private static final String RSA_KEY = "-----BEGIN PUBLIC KEY-----\n"
            + "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAp+hTMDgd82ri2HYIXyTX\n"
            + "kLvCQZ8gpzOrC2Rr8PKAiYfd1F0wXpl83Gt43JHkbWEdoqyhA0oUKwrjEFeIIxJu\n"
            + "bGAJlt1c2ZrFXK8of+XzavOaBp7nyTzuyJKpxSlxSm0S/BakwmaFx3FGCpXDq+wB\n"
            + "vBFJAeRFZFGCcMHcMycjxYAgnJMWi7l8hAEh56RevsLuRBLhelwKRJKoLoZCjzLW\n"
            + "r/4T4Ud/+kpq9vNujCH/TuWTFK0ZkTCkb6XDR9DsFCAB3KJMyODBnyi+jP2fOCYY\n"
            + "irNmndXCVP6H62eEEcJMUsPR6oecQjOs6XC2h/FdxUo76E2rReq4pj46KHoTEb4W\n"
            + "M3nfgG52Dz/pqoE87qHpNMz982JpHUz9Fs+9cITBIrtR0WUG08PdckVRjBwesa1e\n"
            + "GTn59Nd9/tvq9FckiE81+2wacbHjnw+O+cJwGvN5rQy3V+eyQ1lOcpa1ST3Za3Et\n"
            + "XWva2YUjFRzTgv7AOL/TU2n6XnWvd0+YZQIDFxfj4i9fcaYP/9MWZFj7yaMb8f0l\n"
            + "/MSIJqmfkdB4RwSaF6HzHjxHNGh9/14QbS+X/rfBVutC00K65FylAGWwYSnMSnte\n"
            + "D1/zbqCKOLZrMtZFbiUYCZr+quRkzU5b+7FgNpIYEvSBJ8Qb0FAJ1XX6bug3OLnP\n"
            + "fBfo7or/dH+TQs3PkXa6cycCAwEAAQ==\n"
            + "-----END PUBLIC KEY-----\n";

    @Test
    public void testReadPrivateKey() throws IOException {
        PrivateKey key = ECDSAUtils.getPrivateKey(PRIVATE_KEY);

        assertThat(key).isNotNull();
    }

    @Test
    public void testReadPublicKey() throws IOException {
        PublicKey key = ECDSAUtils.getPublicKey(PUBLIC_KEY);

        assertThat(key).isNotNull();
    }

    @Test
    public void testReadPkcs8PrivateKey() throws IOException {
        PrivateKey key = ECDSAUtils.getPrivateKey(PKCS8_PRIVATE_KEY);

        assertThat(key).isNotNull();
    }

    @Test(expected = ClassCastException.class)
    public void testReadInvalidKey() throws IOException {
        PublicKey key = ECDSAUtils.getPublicKey(RSA_KEY);

        assertThat(key).isNotNull();
    }

    @Test
    public void testSignAndVerify() throws Exception {
        byte[] data = "data-to-sign".getBytes();

        Signature signature = ECDSAUtils.getSignature(ECDSAUtils.getPrivateKey(PRIVATE_KEY));
        signature.update(data);

        byte[] signedBytes = signature.sign();

        signature = ECDSAUtils.getSignature(ECDSAUtils.getPublicKey(PUBLIC_KEY));
        signature.update(data);

        assertThat(signature.verify(signedBytes)).isTrue();
    }

    @Test
    public void testSignAndVerifyPkcs8PrivateKey() throws Exception {
        byte[] data = "data-to-sign".getBytes();
        Signature signature = ECDSAUtils.getSignature(ECDSAUtils.getPrivateKey(PKCS8_PRIVATE_KEY));
        signature.update(data);
        byte[] signedBytes = signature.sign();

        signature = ECDSAUtils.getSignature(ECDSAUtils.getPublicKey(PUBLIC_KEY));
        signature.update(data);

        assertThat(signature.verify(signedBytes)).isTrue();
    }


    @Test
    public void testVerifyingWithInvalidPublicKey() throws Exception {
        byte[] data = "good-morning".getBytes();

        Signature signature = ECDSAUtils.getSignature(ECDSAUtils.getPrivateKey(PRIVATE_KEY));
        signature.update(data);
        byte[] signedBytes = signature.sign();

        signature = ECDSAUtils.getSignature(ECDSAUtils.getPublicKey(WRONG_PUBLIC_KEY));
        signature.update(data);
        assertThat(signature.verify(signedBytes)).isFalse();
    }
}
