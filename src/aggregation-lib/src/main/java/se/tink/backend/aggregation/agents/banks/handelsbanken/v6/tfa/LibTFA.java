package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.tfa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.ActivateProfileRequest;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.ActivateProfileResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.AuthorizeRequest;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.CallengeResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.ChallengeRequest;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.CreateProfileRequest;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.CreateProfileResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.DeviceInfoEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.HandshakeContent;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.HandshakeRequest;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.HandshakeResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.InitNewProfileRequest;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.InitNewProfileResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.ServerProfileRequest;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.UserCredentialsRequest;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.ValidateSignatureRequest;

public class LibTFA {
    private static final String APP_ID = "UmUsM5dTKaClsKjKOYRv7o4tXQ3rn9fDxFeCB0b8BpQ=";
    private static final Base64 BASE64_CODEC = new Base64();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final SecureRandom RANDOM = new SecureRandom();

    private static String getPrivkey(KeyPair key) {
        return BASE64_CODEC.encodeAsString(key.getPrivate().getEncoded());
    }

    private static String getPubkey(KeyPair key) {
        return BASE64_CODEC.encodeAsString(key.getPublic().getEncoded());
    }

    public static KeyPair loadDeviceRSAKey(byte[] key) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privKey = kf.generatePrivate(keySpec);

            return new KeyPair(null, privKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
    
    // Don't change! (requires migration)
    private final static String PAYLOAD_PROFILE_ID = "profileId";
    private final static String PAYLOAD_PUBLIC_KEY = "publicKey";
    private final static String PAYLOAD_PRIVATE_KEY = "privateKey";
    private final static String PAYLOAD_DEVICE_SERVER_PROFILE = "pdeviceServerProfile";
    private final static String PAYLOAD_DEVICE_SECURITY_CONTEXT_ID = "deviceSecurityContextId";

    private String deviceSecurityContextId;
    private byte[] cnonce;
    private KeyPair drsakey;
    private KeyPair handshakeKey;
    private String personalId;
    private byte[] profileId;
    private byte[] sessionkey;
    private byte[] signChallenge;
    private byte[] snonceSecret;
    private String pdeviceServerProfile;

    public LibTFA(String personalId) {
        this.personalId = personalId;

        byte[] dscSeed = new byte[32];
        RANDOM.nextBytes(dscSeed);
        this.deviceSecurityContextId = generateDevSecContextId(dscSeed);
    }

    public LibTFA(String personalId, String credentialId) {
        this.personalId = personalId;
        this.deviceSecurityContextId = generateDevSecContextId(credentialId.getBytes(Charsets.UTF_8));
    }

    public void calculateSessionKey(byte[] snonce) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(snonce, "HmacSHA256");
            hmac.init(secret_key);

            for (int i = 0; i < 50000; i++) {
                hmac.update(cnonce);
            }

            sessionkey = hmac.doFinal();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public ActivateProfileRequest createActivateProfileRequest() {
        try {
            generateDeviceRSAKey();

            DeviceInfoEntity deviceInfo = new DeviceInfoEntity(deviceSecurityContextId);

            byte[] deviceInfoSerialized = MAPPER.writeValueAsBytes(deviceInfo);

            String signature = signData(deviceInfoSerialized);

            ActivateProfileRequest request = new ActivateProfileRequest(BASE64_CODEC.encodeAsString(deviceInfoSerialized),
                    signature);

            request.setPubkey(getPubkey(drsakey));

            return request;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public AuthorizeRequest createAuthorizeRequest() {
        return new AuthorizeRequest();
    }

    public ChallengeRequest createChallengeRequest() {
        return new ChallengeRequest(BASE64_CODEC.encodeAsString(cnonce));
    }

    public CreateProfileRequest createCreateProfileRequest(String code) {
        try {
            return new CreateProfileRequest(encryptData(MAPPER.writeValueAsBytes(new UserCredentialsRequest(personalId,
                    code))));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public HandshakeRequest createHandshakeRequest() {
        return new HandshakeRequest(generateCnonce(), generateHandshakeKey());
    }

    public InitNewProfileRequest createInitNewProfileRequest() {
        InitNewProfileRequest r = new InitNewProfileRequest();

        r.setAppId(APP_ID);
        r.setAuthTp("1");
        r.setCnonce(generateCnonce());
        r.setDeviceSecurityContextId(deviceSecurityContextId);
        r.setProfileTransformationTp("1");
        r.setHandshakeKey(generateHandshakeKey());

        return r;
    }

    public ServerProfileRequest createServerProfileRequest(String pincode) {
        try {
            UserCredentialsRequest creds = new UserCredentialsRequest(personalId, pincode);

            ServerProfileRequest request = new ServerProfileRequest();

            request.setProfileId(BASE64_CODEC.encodeAsString(profileId));
            request.setEncUserCredentials(encryptData(MAPPER.writeValueAsBytes(creds)));

            return request;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public ValidateSignatureRequest createValidateSignatureRequest() {
        try {
            DeviceInfoEntity deviceInfo = new DeviceInfoEntity(deviceSecurityContextId);

            byte[] deviceInfoSerialized = MAPPER.writeValueAsBytes(deviceInfo);

            String signature = signData(deviceInfoSerialized);

            return new ValidateSignatureRequest(BASE64_CODEC.encodeAsString(deviceInfoSerialized), signature);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public String decryptData(byte[] encryptedData) throws NoSuchAlgorithmException, NoSuchProviderException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");

        IvParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOfRange(snonceSecret, 0, 16));
        SecretKey keyValue = new SecretKeySpec(sessionkey, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keyValue, ivSpec);

        byte[] decryptedData = cipher.doFinal(encryptedData);
        return BASE64_CODEC.encodeToString(decryptedData);
    }

    public byte[] decryptSnonce(byte[] snonce) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");

            cipher.init(Cipher.DECRYPT_MODE, handshakeKey.getPrivate());
            snonceSecret = cipher.doFinal(snonce);

            return snonceSecret;
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException |
                IllegalBlockSizeException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public String encryptData(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
            IvParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOfRange(snonceSecret, 0, 16));
            SecretKey keyValue = new SecretKeySpec(sessionkey, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keyValue, ivSpec);

            return BASE64_CODEC.encodeToString(cipher.doFinal(data));
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException |
        NoSuchPaddingException | BadPaddingException | NoSuchProviderException | IllegalBlockSizeException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private String generateCnonce() {
        cnonce = new byte[32];
        RANDOM.nextBytes(cnonce);

        return BASE64_CODEC.encodeAsString(cnonce);
    }

    private void generateDeviceRSAKey() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);

            drsakey = keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private String generateDevSecContextId(byte[] seed) {
        // The Device Security Context Id must be:
        //  - 32 bytes long
        //  - The same for both activation and login
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(seed);
            return BASE64_CODEC.encodeAsString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private String generateHandshakeKey() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);

            handshakeKey = keyGen.generateKeyPair();

            return getPubkey(handshakeKey);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public Map<String, String> getPayload() {
        Map<String, String> payload = Maps.newHashMap();
        payload.put(PAYLOAD_PROFILE_ID, BASE64_CODEC.encodeAsString(profileId));
        payload.put(PAYLOAD_PUBLIC_KEY, getPubkey(drsakey));
        payload.put(PAYLOAD_PRIVATE_KEY, getPrivkey(drsakey));
        payload.put(PAYLOAD_DEVICE_SERVER_PROFILE, pdeviceServerProfile);
        payload.put(PAYLOAD_DEVICE_SECURITY_CONTEXT_ID, deviceSecurityContextId);
        return payload;
    }

    public void handleActivateProfileResponse(ActivateProfileResponse activateProfileResponse) {
        profileId = BASE64_CODEC.decode(activateProfileResponse.getProfileId());
    }

    public void handleChallengeResponse(CallengeResponse challengeResponse) {
        signChallenge = BASE64_CODEC.decode(challengeResponse.getChallenge());
    }

    public void handleCreateProfileResponse(CreateProfileResponse createProfileResponse) {
        signChallenge = BASE64_CODEC.decode(createProfileResponse.getChallenge());
        pdeviceServerProfile = createProfileResponse.getPdeviceServerProfile();
    }

    public void handleHandshakeResponse(HandshakeResponse data) {
        try {
            HandshakeContent handshakeContent = MAPPER.readValue(
                    new String(decryptSnonce(BASE64_CODEC.decode(data.getServerHello())), Charsets.US_ASCII), HandshakeContent.class);

            snonceSecret = BASE64_CODEC.decode(handshakeContent.getServerHello().getSnonce());

            calculateSessionKey(snonceSecret);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void handleInitNewProfileResponse(InitNewProfileResponse initNewProfileResponse) {
        byte[] snonce = decryptSnonce(BASE64_CODEC.decode(initNewProfileResponse.getSnonce()));

        calculateSessionKey(snonce);
    }

    public void loadPayload(Map<String, String> payload) {
        profileId = BASE64_CODEC.decode(payload.get(PAYLOAD_PROFILE_ID));
        drsakey = loadDeviceRSAKey(BASE64_CODEC.decode(payload.get(PAYLOAD_PRIVATE_KEY)));
        if (payload.containsKey(PAYLOAD_DEVICE_SECURITY_CONTEXT_ID)) {
            // The key `PAYLOAD_DEVICE_SECURITY_CONTEXT_ID` is a new addition and is not present for all credentials.
            // In the case where it's not present the initial value (set in the constructor) of DSC will be used.
            deviceSecurityContextId = payload.get(PAYLOAD_DEVICE_SECURITY_CONTEXT_ID);
        }
    }

    public String signData(byte[] data) {
        try {
            MessageDigest h = MessageDigest.getInstance("SHA-256");
            byte[] calc_devinfo_digest = h.digest(data);

            Signature s = Signature.getInstance("SHA256withRSA");
            s.initSign(drsakey.getPrivate());
            s.update(snonceSecret);
            s.update(signChallenge);
            s.update(calc_devinfo_digest);
            byte[] signature = s.sign();

            return BASE64_CODEC.encodeAsString(signature);
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
