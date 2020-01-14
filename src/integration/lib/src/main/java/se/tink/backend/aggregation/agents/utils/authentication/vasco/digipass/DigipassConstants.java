package se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass;

public class DigipassConstants {
    public static class CommunicationCrypto {
        public static final String EC_CURVE_NAME = "secp256r1";
        public static final int NONCE_LENGTH = 4;
        public static final int IV_LENGTH = 16;
    }

    public static class StaticVectorFieldType {
        public static final int SIGNATURE = 1;
        public static final int CRYPTO_KEY = 2;
        public static final int DIVERSIFIER_LENGTH = 70;
        public static final int USE_WHITEBOX_CRYPTO = 71;
        public static final int INITIAL_VALUE = 60;
    }

    public static class FixedKeys {
        public static final String INITIAL_CRYPTO_KEY = "47db9d1d0eff9c1cadfb787404ddc249";
        public static final String NULL_CHALLENGE = "0000000000000000";
    }

    public static class Serialization {
        public static final String OTP_COUNTER = "otpCounter";
        public static final String DYNAMIC_VECTOR = "dynamicVector";
        public static final String STATIC_VECTOR = "staticVector";
        public static final String ACTIVATION_MESSAGE2 = "activationMessage2";
        public static final String DIGIPASS_ID = "digipassId";
        public static final String DEVICE_COUNTER = "deviceCounter";
        public static final String KEY_0 = "key0";
        public static final String KEY_1 = "key1";
        public static final String KEY_2 = "key2";
        public static final String KEY_2_A0 = "key2_a0";
        public static final String KEY_2_A1 = "key2_a1";
        public static final String KEY_3 = "key3";
        public static final String KEY_3_B2 = "key3_b2";
        public static final String KEY_3_B3 = "key3_b3";
        public static final String OTP_KEY = "otpKey";
        public static final String CLIENT_NONCE = "clientNonce";
        public static final String ECDH_SHARED_SECRET = "ecdhSharedSecret";
        public static final String XFAD = "xfad";
        public static final String OTP_SEED_IV = "otpSeedIv";
        public static final String OTP_SEED_DATA = "otpSeedData";
        public static final String FINGERPRINT = "fingerPrint";
        public static final String ACTIVATION_PASSWORD = "activationPassword";
        public static final String EC_KEY_PAIR = "ecKeyPair";
    }
}
