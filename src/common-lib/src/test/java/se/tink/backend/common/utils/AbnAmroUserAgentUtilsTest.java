package se.tink.backend.common.utils;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.common.utils.AbnAmroUserAgentUtils.getPushNotificationsAESIvSpecificationMode;
import static se.tink.backend.common.utils.AbnAmroUserAgentUtils.getPublicKeyFormat;

public class AbnAmroUserAgentUtilsTest {

    @Test
    public void hasNotificationExtension() {
        // iOS + Grip >= 2.0.0 + iOS >= 10.0.0
        assertThat(AbnAmroUserAgentUtils.canAlwaysDecryptNotifications("Grip/2.0.0 (iOS; 10.3.2, iPhone)")).isTrue();

        // Android not allowed
        assertThat(AbnAmroUserAgentUtils.canAlwaysDecryptNotifications("Grip/2.0.0 (android; 10.0.0, Samsung)"))
                .isFalse();

        // Must be Grip > 2.0.0
        assertThat(AbnAmroUserAgentUtils.canAlwaysDecryptNotifications("Grip/1.9.0 (iOS; 10.0.0, iPhone)")).isFalse();

        // Must be iOS version >= 10.0.0
        assertThat(AbnAmroUserAgentUtils.canAlwaysDecryptNotifications("Grip/2.0.0 (iOS; 9.9.9, iPhone)")).isFalse();
    }

    @Test
    public void testPushNotificationsAESIvSpecificationMode() {
        assertThat(getPushNotificationsAESIvSpecificationMode("Grip/2.0.0 (iOS; 10.3.2, iPhone)"))
                .isEqualTo(EncryptionUtils.AES.IvMode.BASED_ON_SECRET_KEY);
        assertThat(getPushNotificationsAESIvSpecificationMode("Grip/2.0.0 (android; 10.0.0, Samsung)"))
                .isEqualTo(EncryptionUtils.AES.IvMode.BASED_ON_SECRET_KEY);

        assertThat(getPushNotificationsAESIvSpecificationMode("Grip/3.9.9 (iOS; 10.3.2, iPhone)"))
                .isEqualTo(EncryptionUtils.AES.IvMode.BASED_ON_SECRET_KEY);
        assertThat(getPushNotificationsAESIvSpecificationMode("Grip/3.9.9 (android; 10.0.0, Samsung)"))
                .isEqualTo(EncryptionUtils.AES.IvMode.BASED_ON_SECRET_KEY);

        assertThat(getPushNotificationsAESIvSpecificationMode("Grip/4.0.0 (iOS; 10.3.2, iPhone)"))
                .isEqualTo(EncryptionUtils.AES.IvMode.RANDOM);
        assertThat(getPushNotificationsAESIvSpecificationMode("Grip/4.0.0 (android; 10.0.0, Samsung)"))
                .isEqualTo(EncryptionUtils.AES.IvMode.RANDOM);

        assertThat(getPushNotificationsAESIvSpecificationMode("Grip/4.0.1 (iOS; 10.3.2, iPhone)"))
                .isEqualTo(EncryptionUtils.AES.IvMode.RANDOM);
        assertThat(getPushNotificationsAESIvSpecificationMode("Grip/4.0.1 (android; 10.0.0, Samsung)"))
                .isEqualTo(EncryptionUtils.AES.IvMode.RANDOM);
    }

    /**
     * All clients < 4.0.0 have generated their key in XML_BASE_64_ENCODED format.
     */
    @Test
    public void testXmlBase64EncodedPublicKeyFormat() {
        assertThat(getPublicKeyFormat("Grip/2.0.0 (iOS; 10.3.2, iPhone)"))
                .isEqualTo(EncryptionUtils.RSA.PublicKeyFormat.XML_BASE_64_ENCODED);
        assertThat(getPublicKeyFormat("Grip/2.0.0 (android; 10.0.0, Samsung)"))
                .isEqualTo(EncryptionUtils.RSA.PublicKeyFormat.XML_BASE_64_ENCODED);

        assertThat(getPublicKeyFormat("Grip/3.9.9 (iOS; 10.3.2, iPhone)"))
                .isEqualTo(EncryptionUtils.RSA.PublicKeyFormat.XML_BASE_64_ENCODED);
        assertThat(getPublicKeyFormat("Grip/3.9.9 (android; 10.0.0, Samsung)"))
                .isEqualTo(EncryptionUtils.RSA.PublicKeyFormat.XML_BASE_64_ENCODED);
    }

    /**
     * All clients => 4.0.0 have generated their key in PEM_BASE_64_ENCODED format.
     */
    @Test
    public void testPemBase64EncodedPublicKeyFormat() {
        assertThat(getPublicKeyFormat("Grip/4.0.0 (iOS; 10.3.2, iPhone)"))
                .isEqualTo(EncryptionUtils.RSA.PublicKeyFormat.PEM_BASE_64_ENCODED);
        assertThat(getPublicKeyFormat("Grip/4.0.0 (android; 10.0.0, Samsung)"))
                .isEqualTo(EncryptionUtils.RSA.PublicKeyFormat.PEM_BASE_64_ENCODED);

        assertThat(getPublicKeyFormat("Grip/4.0.1 (iOS; 10.3.2, iPhone)"))
                .isEqualTo(EncryptionUtils.RSA.PublicKeyFormat.PEM_BASE_64_ENCODED);
        assertThat(getPublicKeyFormat("Grip/4.0.1 (android; 10.0.0, Samsung)"))
                .isEqualTo(EncryptionUtils.RSA.PublicKeyFormat.PEM_BASE_64_ENCODED);
    }
}
