package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator;

import com.google.common.base.Strings;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.EnrollmentState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.EnrollmentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.entity.KeyEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.AuthDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.AuthDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.ConfirmEnrollmentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.ConfirmEnrollmentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.EnrollmentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.EnrollmentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.InitDeviceAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.PasswordTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.PasswordTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.VerifyPersonalCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.VerifyPersonalCodeResponse;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.strings.StringUtils;

public class NordeaPasswordAuthenticator implements TypedAuthenticator, AutoAuthenticator {

    private static final Logger log = LoggerFactory.getLogger(NordeaPasswordAuthenticator.class);
    private final NordeaBaseApiClient apiClient;
    private final Credentials credentials;
    private final CredentialsRequest request;
    private final BankIdAuthenticationController bankIdAuthenticationController;
    private final PersistentStorage persistentStorage;
    private final NordeaBankIdSignHelper signHelper;
    private final SessionStorage sessionStorage;
    private KeyPair keyPair;
    private NordeaConfiguration nordeaConfiguration;

    public NordeaPasswordAuthenticator(
            CredentialsRequest request,
            SupplementalInformationController supplementalInformationController,
            NordeaBaseApiClient apiClient,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            BankIdAuthenticationController bankIdAuthenticationController,
            NordeaConfiguration nordeaConfiguration) {

        this.apiClient = apiClient;
        this.request = request;
        this.credentials = request.getCredentials();
        this.bankIdAuthenticationController = bankIdAuthenticationController;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.signHelper = new NordeaBankIdSignHelper(apiClient, supplementalInformationController);
        this.nordeaConfiguration = nordeaConfiguration;

        if (persistentStorage.containsKey(StorageKeys.PERSONAL_CODE_ENROLLMENT_ID)) {
            String privateKeyEncoded = persistentStorage.get(StorageKeys.DEVICE_PRIVATE_KEY);
            String publicKeyEncoded = persistentStorage.get(StorageKeys.DEVICE_PUBLIC_KEY);
            PrivateKey privateKey =
                    RSA.getPrivateKeyFromBytes(EncodingUtils.decodeBase64String(privateKeyEncoded));
            PublicKey publicKey =
                    RSA.getPubKeyFromBytes(EncodingUtils.decodeBase64String(publicKeyEncoded));
            keyPair = new KeyPair(publicKey, privateKey);
        }
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        String password = credentials.getField(Key.PASSWORD);
        if (Strings.isNullOrEmpty(password)) {
            password = this.credentials.getSensitivePayload(StorageKeys.SENSITIVE_PAYLOAD_PASSWORD);
            if (Strings.isNullOrEmpty(password)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
        }

        if (!isPersonalCodeLoginActivated() || shouldReactivatePersonalCodeLogin()) {
            activatePersonalCodeLogin(credentials);
        }
        login(password);
        this.credentials.setSensitivePayload(StorageKeys.SENSITIVE_PAYLOAD_PASSWORD, password);
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, BankServiceException, AuthorizationException {
        // TODO use persistent storage
        String storedPassword =
                this.credentials.getSensitivePayload(StorageKeys.SENSITIVE_PAYLOAD_PASSWORD);
        if (!isPersonalCodeLoginActivated() || Strings.isNullOrEmpty(storedPassword)) {
            log.warn("Automatic refresh, but has no password.");
            throw SessionError.SESSION_EXPIRED.exception();
        }
        if (shouldReactivatePersonalCodeLogin() && request.isManual()) {
            // fallback to manual authentication if user device enrollment is about to expire
            // and the request is manual
            // if request is automatic try to login even if enrollment expired
            throw SessionError.SESSION_EXPIRED.exception();
        }

        try {
            login(storedPassword);
        } catch (LoginException e) {
            // fallback to manual authentication if possible
            log.warn("Auto Login Failed: " + e.getMessage(), e);
            throw SessionError.SESSION_EXPIRED.exception(e);
        }
    }

    private void login(String password) throws LoginException {
        String codeVerifier = EncodingUtils.encodeAsBase64UrlSafe(RandomUtils.secureRandom(64));
        // authenticate device
        String deviceToken = authenticateDevice(codeVerifier);
        sessionStorage.put(StorageKeys.DEVICE_AUTH_TOKEN, deviceToken);

        VerifyPersonalCodeRequest verifyRequest =
                VerifyPersonalCodeRequest.create(password, deviceToken);
        // verify personal code
        VerifyPersonalCodeResponse verifyResponse = verifyPersonalCode(verifyRequest);
        // get access token
        PasswordTokenResponse tokenResponse =
                getAccessToken(codeVerifier, verifyResponse.getAuthorizationCode());
        tokenResponse.storeTokens(sessionStorage);
    }

    private String authenticateDevice(String codeVerifier) throws LoginException {
        try {
            String enrollmentId = persistentStorage.get(StorageKeys.PERSONAL_CODE_ENROLLMENT_ID);
            InitDeviceAuthResponse initResponse = apiClient.initDeviceAuthentication(enrollmentId);
            String serverNonce = initResponse.getNonce();
            String clientNonce = RandomUtils.generateRandomBase64UrlEncoded(32);
            String clearSignature = String.format("%s:%s", serverNonce, clientNonce);
            String challenge = EncodingUtils.encodeAsBase64UrlSafe(Hash.sha256(codeVerifier));
            String signature =
                    EncodingUtils.encodeAsBase64String(
                            RSA.signSha256(keyPair.getPrivate(), clearSignature.getBytes()));

            AuthDeviceResponse authResponse =
                    apiClient.authenticateDevice(
                            AuthDeviceRequest.create(signature, challenge, clientNonce),
                            enrollmentId);
            String deviceToken = authResponse.getToken();
            if (Strings.isNullOrEmpty(deviceToken)) {
                throw LoginError.REGISTER_DEVICE_ERROR.exception();
            }
            return deviceToken;
        } catch (HttpResponseException e) {
            log.warn("Could not authenticate device enrollment, reason: ", e);
            throw LoginError.REGISTER_DEVICE_ERROR.exception(e);
        }
    }

    private VerifyPersonalCodeResponse verifyPersonalCode(VerifyPersonalCodeRequest verifyRequest)
            throws LoginException {
        try {
            return apiClient.verifyPersonalCode(verifyRequest);
        } catch (HttpResponseException e) {
            log.warn("Failed to verify personal code", e);
            if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED) {
                throw LoginError.INCORRECT_CREDENTIALS.exception(e);
            } else {
                throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(e);
            }
        }
    }

    private PasswordTokenResponse getAccessToken(String codeVerifier, String authCode)
            throws LoginException {
        try {
            return apiClient.getPasswordAccessToken(
                    PasswordTokenRequest.of(
                            codeVerifier, authCode, nordeaConfiguration.getClientId()));
        } catch (HttpResponseException e) {
            log.warn("Failed to get Access token", e);
            throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(e);
        }
    }

    /** @return whether the user has activated personal login through Tink */
    private boolean isPersonalCodeLoginActivated() {
        return Objects.nonNull(keyPair);
    }

    /**
     * In the Nordea App, after enabling personal code login it will only be enabled for a specific
     * period of time, after that the user has to re-enable it using mobile bank-id.
     *
     * @return <code>true</code> if the personal code will expire in 14 days or less. <code>false
     *     </code> otherwise
     */
    private boolean shouldReactivatePersonalCodeLogin() {
        Optional<Date> validUntil =
                persistentStorage.get(StorageKeys.PERSONAL_CODE_VALID_UNTIL, Date.class);
        Date date = DateUtils.addDays(DateUtils.getToday(), 14);
        return !validUntil.isPresent() || validUntil.get().before(date);
    }

    private void activatePersonalCodeLogin(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        // to enable bank id login the user have to login with bank id first
        loginWithBankId(credentials);
        keyPair = RSA.generateKeyPair();
        String publicKey = EncodingUtils.encodeAsBase64String(keyPair.getPublic().getEncoded());
        String privateKey = EncodingUtils.encodeAsBase64String(keyPair.getPrivate().getEncoded());
        String deviceKid = EncodingUtils.encodeAsBase64String(Hash.sha1(publicKey.getBytes()));
        try {
            EnrollmentResponse enrollmentResponse = enrollDevice(publicKey, deviceKid);
            String signOrderRef = signHelper.sign(enrollmentResponse.getSigningItemId());
            confirmPersonalCodeEnrollment(enrollmentResponse.getId(), signOrderRef);
            persistentStorage.put(StorageKeys.DEVICE_PUBLIC_KEY, publicKey);
            persistentStorage.put(StorageKeys.DEVICE_PRIVATE_KEY, privateKey);
            persistentStorage.put(
                    StorageKeys.PERSONAL_CODE_ENROLLMENT_ID, enrollmentResponse.getId());
            revokeBankIdToken();
        } catch (HttpResponseException e) {
            log.warn("Failed to activate personal code, reason: ", e);
            throw LoginError.REGISTER_DEVICE_ERROR.exception(e);
        }
    }

    private void loginWithBankId(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        try {
            this.credentials.setType(CredentialsTypes.MOBILE_BANKID);
            bankIdAuthenticationController.authenticate(credentials);
        } finally {
            this.credentials.setType(CredentialsTypes.PASSWORD);
        }
    }

    private void revokeBankIdToken() {
        try {
            apiClient.logout();
        } catch (Exception e) {
            // ignore any exception
        }
        sessionStorage.remove(StorageKeys.ACCESS_TOKEN);
        sessionStorage.remove(StorageKeys.REFRESH_TOKEN);
        sessionStorage.remove(StorageKeys.TOKEN_TYPE);
    }

    private void confirmPersonalCodeEnrollment(String id, String signOrderRef)
            throws LoginException {
        ConfirmEnrollmentResponse response =
                apiClient.confirmEnrollment(new ConfirmEnrollmentRequest(signOrderRef), id);
        if (!response.getState().equals(EnrollmentState.ACTIVE)) {
            throw LoginError.REGISTER_DEVICE_ERROR.exception();
        }
        persistentStorage.put(StorageKeys.PERSONAL_CODE_VALID_UNTIL, response.getValidUntil());
    }

    private EnrollmentResponse enrollDevice(String publicKey, String kid) {
        EnrollmentRequest enrollmentRequest =
                EnrollmentRequest.create(
                        getDeviceName(),
                        EnrollmentType.PERSONAL_CODE,
                        KeyEntity.create(publicKey, kid));

        return apiClient.enrollForPersonalCode(enrollmentRequest);
    }

    private String getDeviceName() {
        return StringUtils.hashAsUUID(
                "TINK-" + this.request.getCredentials().getField(Key.USERNAME));
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }
}
