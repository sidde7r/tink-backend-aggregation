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
        public static final int INITIAL_VALUE = 60;
    }

    public static class Serialization {
        public static final String OTP_COUNTER = "otpCounter";
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
