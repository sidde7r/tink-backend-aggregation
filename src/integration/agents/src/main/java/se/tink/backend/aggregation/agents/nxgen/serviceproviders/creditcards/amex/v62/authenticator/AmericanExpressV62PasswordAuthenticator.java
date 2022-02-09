package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator;

import com.google.common.primitives.Bytes;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.CryptoKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.Storage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.Tags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Predicates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities.JOSEHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities.KeysEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.InitializationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.KeyExchangeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.KeyExchangeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.LogonRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.LogonResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.RSAPayloadRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.RSAPayloadResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.utils.AmericanExpressV62Storage;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.cryptography.AES;
import se.tink.libraries.cryptography.RSA;
import se.tink.libraries.cryptography.hash.Hash;
import se.tink.libraries.encoding.EncodingUtils;
import se.tink.libraries.i18n_aggregation.LocalizableKey;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.strings.StringUtils;

public class AmericanExpressV62PasswordAuthenticator implements PasswordAuthenticator {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AmericanExpressV62PasswordAuthenticator.class);
    private final AmericanExpressV62ApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final AmericanExpressV62Storage instanceStorage;
    private final RandomValueGenerator randomValueGenerator;

    public AmericanExpressV62PasswordAuthenticator(
            AmericanExpressV62ApiClient apiClient,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            final AmericanExpressV62Storage instanceStorage,
            final RandomValueGenerator randomValueGenerator) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.instanceStorage = instanceStorage;
        this.randomValueGenerator = randomValueGenerator;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {

        prepareStorageBeforeAuth(username);
        sendInitializationRequests();
        final String amexPayKey = createJWE();
        final LogonRequest logonRequest = prepareLogon(amexPayKey, username, password);
        processLogon(logonRequest);
    }

    private String createJWE() {
        final byte[] random32BytesKey = RandomUtils.secureRandom(32);
        final RSAPayloadResponse decryptedKeys = decryptEncryptedKeys();

        final String keyEncryptionVersion =
                decryptedKeys.getKeys().stream()
                        .filter(k -> "MSL_FIELD_ENCRYPTION".equalsIgnoreCase(k.getKeyPurpose()))
                        .findFirst()
                        .map(KeysEntity::getKid)
                        .orElseThrow(
                                () ->
                                        new NullPointerException(
                                                "Could either not find the MSL_FIELD_ENCRYPTION key or the key id. Try to trace the app to find out if it uses a different key for key wrapping"));
        final KeysEntity mslKey =
                decryptedKeys.getKeys().stream()
                        .filter(k -> "MSL_ENCRYPTION".equalsIgnoreCase(k.getKeyPurpose()))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new NullPointerException(
                                                "Could not find MSL_ENCRYPTION key. Try to trace the app to find out if it uses a different key for key wrapping"));

        final byte[] joseHeaderAsBytes =
                new JOSEHeaderEntity(mslKey.getKid(), keyEncryptionVersion).toString().getBytes();
        final String joseHeaderAsBase64String =
                Base64.getEncoder().encodeToString(joseHeaderAsBytes);
        final String EncryptedKeysAsBase64String = createJWEEncryptedKey(mslKey, random32BytesKey);
        final String cipherAsBase64String =
                createJWECipher(Arrays.copyOfRange(random32BytesKey, 16, 32));
        final String hmacAsBase64String =
                createJWEHmac(
                        Arrays.copyOfRange(random32BytesKey, 0, 16),
                        joseHeaderAsBase64String,
                        cipherAsBase64String);

        return String.format(
                "%s.%s.%s.%s.%s",
                joseHeaderAsBase64String,
                EncryptedKeysAsBase64String,
                CryptoKeys.ZERO_IV_BASE64,
                cipherAsBase64String,
                hmacAsBase64String);
    }

    // AES-256 CBC decrypt, key is a symmetric key which is the bytes of the client-generated hex
    // string. IV is 16 zero bytes.
    private RSAPayloadResponse decryptEncryptedKeys() {
        final String random32CharHex = persistentStorage.get(Storage.RANDOM_HEX);
        final String encryptedKeysAsBase64 = persistentStorage.get(Storage.ENCRYPTED_KEYS);
        final byte[] random32BytesKey = random32CharHex.getBytes();
        final byte[] zeroBytesIvAsBytes = Base64.getDecoder().decode(CryptoKeys.ZERO_IV_BASE64);
        final byte[] encryptedKeysAsBytes = Base64.getDecoder().decode(encryptedKeysAsBase64);
        final byte[] decryptedKeysAsBytes =
                AES.decryptCbc(random32BytesKey, zeroBytesIvAsBytes, encryptedKeysAsBytes);
        return SerializationUtils.deserializeFromBytes(
                decryptedKeysAsBytes, RSAPayloadResponse.class);
    }

    // Key wrapping of the client generated 32 bytes
    private String createJWEEncryptedKey(KeysEntity mslKey, byte[] random32BytesKey) {
        final String mslKeyAsBase64String = mslKey.getKey();
        final byte[] msl32BytesKey = Base64.getDecoder().decode(mslKeyAsBase64String);
        try {
            final Cipher instance = Cipher.getInstance("AESWrap");
            final Key secretKeySpec = new SecretKeySpec(random32BytesKey, "AES");
            final Key secretKeySpec2 = new SecretKeySpec(msl32BytesKey, "AES");
            instance.init(3, secretKeySpec);
            final byte[] resultAsBytes = instance.wrap(secretKeySpec2);
            return Base64.getEncoder().encodeToString(resultAsBytes);
        } catch (InvalidKeyException
                | IllegalBlockSizeException
                | NoSuchPaddingException
                | NoSuchAlgorithmException e) {
            throw new RuntimeException("Error encrypting", e);
        }
    }

    // AES-128 CBC encrypt of random 64 hex characters
    private String createJWECipher(byte[] random16BytesKey) {
        final byte[] zeroBytesIvAsBytes = Base64.getDecoder().decode(CryptoKeys.ZERO_IV_BASE64);
        final String random64HexCharacters = RandomUtils.generateRandomHexEncoded(32);
        final byte[] cipherAsBytes =
                AES.encryptCbc(
                        random16BytesKey, zeroBytesIvAsBytes, random64HexCharacters.getBytes());
        return Base64.getEncoder().encodeToString(cipherAsBytes);
    }

    private String createJWEHmac(
            byte[] random16BytesKey,
            String joseHeaderAsBase64String,
            String ciperJWEAsBase64String) {
        final byte[] joseHeaderAsBytes = joseHeaderAsBase64String.getBytes();
        final byte[] ivJWEAsBytes = Base64.getDecoder().decode(CryptoKeys.ZERO_IV_BASE64);
        final byte[] cipherJWEAsBytes = Base64.getDecoder().decode(ciperJWEAsBase64String);
        final byte[] hmacSaltAsBytes = Base64.getDecoder().decode(CryptoKeys.HMAC_SALT_BASE64);
        final byte[] hmacDataAsBytes =
                Bytes.concat(joseHeaderAsBytes, ivJWEAsBytes, cipherJWEAsBytes, hmacSaltAsBytes);
        final byte[] hmacJWEAsBytes = Hash.hmacSha256(random16BytesKey, hmacDataAsBytes);

        return Base64.getEncoder().encodeToString(Arrays.copyOfRange(hmacJWEAsBytes, 0, 16));
    }

    private LogonRequest prepareLogon(String amexPayKey, String username, String password) {
        String rememberMeToken = persistentStorage.get(Tags.REMEMBER_ME_TOKEN);
        String maskedUserId = persistentStorage.get(Tags.MASKED_USER_ID);
        if (rememberMeToken != null && maskedUserId != null) {
            return new LogonRequest(amexPayKey, maskedUserId, password, rememberMeToken);
        }

        return new LogonRequest(amexPayKey, username, password);
    }

    private void processLogon(LogonRequest request)
            throws AuthenticationException, AuthorizationException {
        LogonResponse response = apiClient.logon(request);
        if (!response.isSuccess()) {
            sessionStorage.clear();
            persistentStorage.clear();
            handleErrorResponse(response);
        }
        prepareStorageAfterAuth(response);
    }

    private void handleErrorResponse(LogonResponse response)
            throws AuthenticationException, AuthorizationException {

        String reportingCode = response.getStatus().getReportingCode().toUpperCase();
        switch (reportingCode) {
            case AmericanExpressV62Constants.ReportingCode.LOGON_FAIL_FIRST_ATTEMPT:
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            case AmericanExpressV62Constants.ReportingCode.LOGON_FAIL_SECOND_ATTEMPT:
                throw LoginError.INCORRECT_CREDENTIALS_LAST_ATTEMPT.exception();
            case AmericanExpressV62Constants.ReportingCode.LOGON_FAIL_ACCOUNT_BLOCKED:
                throw AuthorizationError.ACCOUNT_BLOCKED.exception();
            case AmericanExpressV62Constants.ReportingCode.LOGON_FAIL_CONTENT_ERROR:
                // This error might be fixed by updating headers
                LOGGER.error(
                        String.format("%s: %s", reportingCode, response.getStatus().getMessage()));
                throw new IllegalStateException("Login failure - check headers");
            case AmericanExpressV62Constants.ReportingCode.BANKSIDE_TEMPORARY_ERROR:
                throw BankServiceError.BANK_SIDE_FAILURE.exception(
                        "Error code: "
                                + reportingCode
                                + ", error message: "
                                + response.getStatus().getMessage());
            case AmericanExpressV62Constants.ReportingCode.UNSUPPORTED_MARKET:
                // Using the message sent from Amex as user message as it will be in the local
                // language and with
                // reference to the relevant market.
                throw LoginError.NOT_CUSTOMER.exception(
                        new LocalizableKey(response.getStatus().getMessage()));

            default:
                LOGGER.error(
                        String.format("%s: %s", reportingCode, response.getStatus().getMessage()));
                throw new IllegalStateException("Logon failure");
        }
    }

    private void sendInitializationRequests() {
        InitializationResponse response = apiClient.initialization();
        keyExchange();
        apiClient.fetchSaneIdCookie();
        if (response.getInitialization().getStatus().equals(0)) {
            persistentStorage.put(Tags.INIT_VERSION, response.getInitialization().getVersion());
        }
    }

    private void keyExchange() {
        final String randomKeyAsHexString = persistentStorage.get(Storage.RANDOM_HEX);
        final RSAPayloadRequest rsaPayload = new RSAPayloadRequest(randomKeyAsHexString);
        final String payloadAsBytes = SerializationUtils.serializeToString(rsaPayload);
        assert payloadAsBytes != null;
        final byte[] rsaPublicKeyAsBytes =
                EncodingUtils.decodeBase64String(CryptoKeys.RSA_PUBLIC_KEY);
        final RSAPublicKey rsaPublicKey = RSA.getPubKeyFromBytes(rsaPublicKeyAsBytes);
        final byte[] encryptedMessageAsBytes =
                RSA.encryptNonePkcs1(rsaPublicKey, payloadAsBytes.getBytes());
        final String encryptedMessageAsString =
                EncodingUtils.encodeAsBase64String(encryptedMessageAsBytes);
        final KeyExchangeRequest keyExchangeRequest =
                new KeyExchangeRequest(encryptedMessageAsString);
        final KeyExchangeResponse keyExchangeResponse = apiClient.keyExchange(keyExchangeRequest);

        // Client receives the encrypted keys only once
        if ("success".equalsIgnoreCase(keyExchangeResponse.getStatusCode())
                && "success".equalsIgnoreCase(keyExchangeResponse.getStatusCodeType())) {
            persistentStorage.put(Storage.ENCRYPTED_KEYS, keyExchangeResponse.getEncryptedKeys());
        }
    }

    private void prepareStorageBeforeAuth(String username) {
        persistentStorage.computeIfAbsent(
                Storage.RANDOM_HEX, k -> generateUUID().replaceAll("-", ""));
        persistentStorage.computeIfAbsent(
                AmericanExpressV62Constants.Tags.HARDWARE_ID,
                k -> StringUtils.hashAsUUID(username));
        persistentStorage.computeIfAbsent(
                AmericanExpressV62Constants.Tags.INSTALLATION_ID, k -> generateUUID());
        persistentStorage.computeIfAbsent(
                AmericanExpressV62Constants.Tags.PROCESS_ID, k -> generateUUID());
    }

    private void prepareStorageAfterAuth(LogonResponse response) {
        sessionStorage.put(
                AmericanExpressV62Constants.Tags.CUPCAKE, response.getLogonData().getCupcake());
        sessionStorage.put(
                AmericanExpressV62Constants.Tags.SESSION_ID,
                response.getLogonData().getAmexSession());
        sessionStorage.put(
                AmericanExpressV62Constants.Tags.USER_DATA,
                response.getSummaryData().getUserData());
        sessionStorage.put(
                AmericanExpressV62Constants.Tags.GATEKEEPER,
                response.getLogonData().getGateKeeperCookie());
        sessionStorage.put(
                AmericanExpressV62Constants.Tags.AUTHORIZATION,
                createAuthorizationHeaderValue(response));

        persistentStorage.putIfAbsent(
                AmericanExpressV62Constants.Tags.MASKED_USER_ID,
                response.getLogonData().getProfileData().getMaskedUserId());
        persistentStorage.putIfAbsent(
                AmericanExpressV62Constants.Tags.REMEMBER_ME_TOKEN,
                response.getLogonData().getProfileData().getData());
        persistentStorage.putIfAbsent(
                AmericanExpressV62Constants.Tags.PUBLIC_GUID,
                response.getLogonData().getPublicGuid());

        instanceStorage.saveCreditCardList(getCardList(response));
    }

    private List<CardEntity> getCardList(LogonResponse response) {
        return response.getSummaryData().getCardList().stream()
                .filter(AmericanExpressV62Predicates.cancelledCardsPredicate)
                // Double filtering for backward compatibility
                .filter(AmericanExpressV62Predicates.cancelledCardSummaryValuePredicate)
                .collect(Collectors.toList());
    }

    private String generateUUID() {
        return randomValueGenerator.getUUID().toString().toUpperCase();
    }

    private String createAuthorizationHeaderValue(LogonResponse response) {
        String rawToken = response.getLogonData().getJsonWebToken().getRawToken();
        return "Bearer " + rawToken;
    }
}
