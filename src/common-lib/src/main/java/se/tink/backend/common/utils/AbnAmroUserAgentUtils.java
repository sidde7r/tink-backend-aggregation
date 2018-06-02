package se.tink.backend.common.utils;

import com.google.common.base.Preconditions;
import se.tink.backend.core.Device;
import se.tink.backend.core.TinkUserAgent;

public class AbnAmroUserAgentUtils {

    /**
     * This will return true if the the user is using at least iOS 10.0.0 and at least Grip 2.0.0. Apple added a new
     * Notification Service Extension which is used for decrypting encrypted notifications in iOS 10 and it was
     * implemented in Grip 2.0.0.
     * - IOS 9.8.0 and Grip 2.1.1 will return false since the service extension isn't available in in IOS < 10.0.0.
     * - IOS 10.3.0 and Grip 1.8.1 will return false since the service extension was added in Grip 2.0.0.
     */
    public static boolean canAlwaysDecryptNotifications(String userAgent) {
        Preconditions.checkNotNull(userAgent);

        TinkUserAgent tinkUserAgent = new TinkUserAgent(userAgent);

        return tinkUserAgent.isIOS() && tinkUserAgent.hasMinimumOsVersion("10.0.0") && tinkUserAgent
                .hasValidIosAppVersion("2.0.0");
    }

    /**
     * This will return which AES IvMode specification mode that is implemented in the client/device. Grip apps for iOS
     * and Android higher than or equal to 4.0.0 supports random byte IV. Older apps require the IV to be based on the
     * SecretKey. The SecretKey implementation is deprecated and will be removed when the older apps are deprecated.
     */
    public static EncryptionUtils.AES.IvMode getPushNotificationsAESIvSpecificationMode(String userAgent) {
        Preconditions.checkNotNull(userAgent);

        TinkUserAgent tinkUserAgent = new TinkUserAgent(userAgent);

        // Android and iOS at minimum version 4.0.0
        if (tinkUserAgent.hasValidVersion("4.0.0", "4.0.0")) {
            return EncryptionUtils.AES.IvMode.RANDOM;
        } else {
            return EncryptionUtils.AES.IvMode.BASED_ON_SECRET_KEY;
        }
    }

    /**
     * Return which format the RSA keys are generated in from the client. We used XML for the Xamarin apps and PEM
     * format for the native iOS and Android apps.
     */
    public static EncryptionUtils.RSA.PublicKeyFormat getPublicKeyFormat(String userAgent) {
        Preconditions.checkNotNull(userAgent);

        TinkUserAgent tinkUserAgent = new TinkUserAgent(userAgent);

        // Android and iOS at minimum version 4.0.0 send the RSA keys in PEM format
        if (tinkUserAgent.hasValidVersion("4.0.0", "4.0.0")) {
            return EncryptionUtils.RSA.PublicKeyFormat.PEM_BASE_64_ENCODED;
        } else {
            return EncryptionUtils.RSA.PublicKeyFormat.XML_BASE_64_ENCODED;
        }
    }

    public static boolean canAlwaysDecryptNotifications(Device device) {
        Preconditions.checkNotNull(device);
        return canAlwaysDecryptNotifications(device.getUserAgent());
    }
}
