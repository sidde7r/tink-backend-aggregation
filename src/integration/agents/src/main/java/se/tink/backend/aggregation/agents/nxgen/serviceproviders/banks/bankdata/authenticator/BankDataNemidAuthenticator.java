package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator;

import com.google.common.base.Strings;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConstants.Authentication;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConstants.Crypto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConstants.JwtKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConstants.JwtValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.ChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.ClientRegistrationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.JweRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.OAuthResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParameters;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParametersFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameController;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.cryptography.RSA;
import se.tink.libraries.cryptography.hash.Hash;
import se.tink.libraries.encoding.EncodingUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
@AllArgsConstructor
@Slf4j
public class BankDataNemidAuthenticator
        implements MultiFactorAuthenticator, AutoAuthenticator, NemIdParametersFetcher {
    private final BankDataApiClient apiClient;
    private final BankDataPersistentStorage jyskePersistentStorage;
    private final RandomValueGenerator randomValueGenerator;
    private final SessionStorage sessionStorage;
    private NemIdIFrameController iFrameController;

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        checkCredentials(credentials);
        callInitialHttpRequests();
        final Document validateNemIdTokenResponseBody = nemIdAuthenticate(credentials);
        final OAuthResponse oAuthResponse = getOAuthResponse(validateNemIdTokenResponseBody);
        final String accessToken = oAuthResponse.getAccessToken();
        final String refreshToken = oAuthResponse.getRefreshToken();
        sessionStorage.put(Storage.ACCESS_TOKEN, accessToken);
        sessionStorage.put(Storage.REFRESH_TOKEN, refreshToken);
    }

    private OAuthResponse getOAuthResponse(Document validateNemIdTokenResponseBody) {
        final ClientRegistrationResponse clientRegistrationResponse =
                fetchClientSecret(validateNemIdTokenResponseBody);
        final String clientId = clientRegistrationResponse.getClientId();
        final String clientSecret = clientRegistrationResponse.getClientSecret();
        jyskePersistentStorage.setClientId(clientId);
        jyskePersistentStorage.setClientSecret(clientSecret);

        final String password = createEnrollmentPackage(clientId);
        final Form oauthForm = buildOauthTokenForm(clientId, password);

        return apiClient.fetchAccessToken(clientId, clientSecret, oauthForm);
    }

    private Document nemIdAuthenticate(Credentials credentials) {
        final String nemIdToken = iFrameController.logInWithCredentials(credentials);
        return getNemIdTokenResponseBody(nemIdToken);
    }

    private ClientRegistrationResponse fetchClientSecret(Document validateNemidTokenResponseBody) {
        log.info("Building form object from NemId token html response");
        final String uri =
                validateNemidTokenResponseBody
                        .getElementById("form1")
                        .getElementsByAttribute("action")
                        .attr("action");
        final Form form = buildTokenForm(validateNemidTokenResponseBody);
        log.info("Fetching response object to extract access token from the redirect header");
        final String redirect = apiClient.fetchToken(uri, form);
        final String token = getAccessTokenFromUrl(redirect);

        return apiClient.fetchClientSecret(token);
    }

    private Document getNemIdTokenResponseBody(String nemIdToken) {
        log.info("Validating NemId token and parsing KeyId from response cookie");
        final HttpResponse validateNemIdTokenResponse = apiClient.validateNemIdToken(nemIdToken);
        final String keyId =
                validateNemIdTokenResponse.getCookies().stream()
                        .filter(cookie -> HeaderKeys.KEY_ID.equals(cookie.getName()))
                        .findFirst()
                        .map(cookie -> cookie.getValue().substring(0, 32))
                        .orElseThrow(IllegalStateException::new);

        jyskePersistentStorage.setKeyId(keyId);

        final String validateNemidTokenResponseString =
                validateNemIdTokenResponse.getBody(String.class);

        return Jsoup.parse(validateNemidTokenResponseString);
    }

    private void callInitialHttpRequests() {
        final String correlationId = randomValueGenerator.generateUuidWithTinkTag();
        apiClient.validateVersion(correlationId);
        apiClient.serverStatus(correlationId);
        apiClient.generalHealth(correlationId);
    }

    private void checkCredentials(Credentials credentials) {
        final String username = credentials.getField(Field.Key.USERNAME);
        final String password = credentials.getField(Field.Key.PASSWORD);
        final String pin = credentials.getField(Field.Key.ACCESS_PIN);
        if (Strings.isNullOrEmpty(username)
                || Strings.isNullOrEmpty(password)
                || Strings.isNullOrEmpty(pin)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        jyskePersistentStorage.setUserId(username);
        jyskePersistentStorage.setPincode(pin);
    }

    private String getAccessTokenFromUrl(String redirect) {
        final Pattern uuidRegex = Pattern.compile("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})");
        final Matcher matcher = uuidRegex.matcher(redirect);
        if (matcher.find()) {
            return matcher.group();
        } else {
            throw new IllegalStateException("Could not find token from redirect header");
        }
    }

    private Form buildTokenForm(Document responseBody) {
        final Form.Builder formBuilder = Form.builder();
        for (Element e : responseBody.select("input")) {
            formBuilder.put(e.attr("name"), e.attr("value"));
        }
        return formBuilder.build();
    }

    private Form buildOauthTokenForm(String clientId, String password) {
        final String replayId = randomValueGenerator.generateUuidWithTinkTag();

        final Form.Builder formBuilder = Form.builder();
        formBuilder.put(FormKeys.CLIENT_ID, clientId);
        formBuilder.put(FormKeys.GRANT_TYPE, FormValues.PASSWORD);
        formBuilder.put(FormKeys.PASSWORD, password);
        formBuilder.put(FormKeys.SCOPE, FormValues.REPLAY_ID + replayId);
        formBuilder.put(FormKeys.USERNAME, FormValues.NOT_USED);

        return formBuilder.build();
    }

    private String createEnrollmentPackage(String clientId) {
        log.info("Creating enrollment package for manual authentication");
        final JWEAlgorithm alg = JWEAlgorithm.RSA_OAEP_256;
        final EncryptionMethod enc = EncryptionMethod.A128CBC_HS256;

        final KeyPair keyPair = RSA.generateKeyPair(2048);
        final String publicKey =
                EncodingUtils.encodeAsBase64String(keyPair.getPublic().getEncoded());

        jyskePersistentStorage.setKeyPair(keyPair);

        final JWTClaimsSet claimsSet =
                new JWTClaimsSet.Builder()
                        .issuer(clientId)
                        .claim(JwtKeys.PWD, jyskePersistentStorage.getPincode())
                        .claim(JwtKeys.KEY_ID, jyskePersistentStorage.getKeyId())
                        .claim(JwtKeys.ECHA, sessionStorage.get(Storage.CODE_VERIFIER))
                        .claim(JwtKeys.PUB, publicKey)
                        .claim(JwtKeys.KTY, JwtValues.KTY)
                        .claim(JwtKeys.APP, JwtValues.APP)
                        .claim(JwtKeys.AOS, JwtValues.AOS)
                        .build();
        final JweRequest jweRequest =
                new JweRequest(claimsSet.toString(), JwtValues.ENROLLMENT_TYPE);

        return createJWE(alg, enc, jweRequest);
    }

    private String createJWE(JWEAlgorithm alg, EncryptionMethod enc, JweRequest jweRequest) {
        try {
            final KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(enc.cekBitLength());
            final SecretKey cek = keyGenerator.generateKey();

            final String jweData = SerializationUtils.serializeToString(jweRequest);
            final Payload payload = new Payload(jweData);

            final JWEObject jweObject =
                    new JWEObject(
                            new JWEHeader.Builder(alg, enc).keyID(Crypto.KEY_ID).build(), payload);
            final RSAPublicKey rsaPubKey =
                    RSA.getPubKeyFromBytes(EncodingUtils.decodeBase64String(Crypto.PUBLIC_KEY));
            jweObject.encrypt(new RSAEncrypter(rsaPubKey, cek));

            return jweObject.serialize();
        } catch (JOSEException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Couldn't create JWE object", e);
        }
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public NemIdParameters getNemIdParameters() {
        final String codeVerifier = generateCodeVerifier();
        sessionStorage.put(Storage.CODE_VERIFIER, codeVerifier);
        final String codeChallenge = generateCodeChallenge(codeVerifier);
        final HttpResponse nemidInitResponse = apiClient.nemIdInit(codeChallenge);

        return fetchNemIdParameters(nemidInitResponse);
    }

    private NemIdParameters fetchNemIdParameters(HttpResponse nemidInitResponse) {
        final String nemidInitResponseString = nemidInitResponse.getBody(String.class);
        final Document nemidInitResponseBody = Jsoup.parse(nemidInitResponseString);
        final String launcher =
                nemidInitResponseBody
                        .getElementById("nemid_iframe")
                        .getElementsByAttribute("src")
                        .attr("src");

        final String formattedIframe = String.format(Authentication.NEM_ID_IFRAME_FORMAT, launcher);

        final String nemidParametersScriptTag =
                nemidInitResponseBody.getElementById("nemid_parameters").toString();
        final String nemidParametersElement = nemidParametersScriptTag + formattedIframe;

        return new NemIdParameters(nemidParametersElement);
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, LoginException, BankServiceException, AuthorizationException {
        callInitialHttpRequests();
        final String kid = jyskePersistentStorage.getKeyId();
        final String clientId = jyskePersistentStorage.getClientId();
        final String clientSecret = jyskePersistentStorage.getClientSecret();

        if (Strings.isNullOrEmpty(kid)
                || Strings.isNullOrEmpty(clientId)
                || Strings.isNullOrEmpty(clientSecret)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        final ChallengeResponse challengeResponse = apiClient.fetchChallengeCode(kid);
        final String challenge = challengeResponse.getChallenge();

        final String password = createLoginPackage(kid, clientId, challenge);
        final Form oauthForm = buildOauthTokenForm(clientId, password);

        try {
            final OAuthResponse oAuthResponse =
                    apiClient.fetchAccessToken(clientId, clientSecret, oauthForm);
            final String accessToken = oAuthResponse.getAccessToken();
            final String refreshToken = oAuthResponse.getRefreshToken();
            sessionStorage.put(Storage.ACCESS_TOKEN, accessToken);
            sessionStorage.put(Storage.REFRESH_TOKEN, refreshToken);
        } catch (HttpResponseException e) {
            final String error = e.getResponse().getBody(ErrorResponse.class).getError();
            if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST
                    && "invalid_grant".equals(error)) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
            throw e;
        }
    }

    private String createLoginPackage(String kid, String clientId, String challenge) {
        try {
            log.info("Creating login package for auto authentication");
            final JWSHeader header =
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .keyID(kid)
                            .type(JOSEObjectType.JWT)
                            .build();

            final JWTClaimsSet claimsSet =
                    new JWTClaimsSet.Builder()
                            .issuer(clientId)
                            .claim(JwtKeys.CHAL, challenge)
                            .claim(JwtKeys.COUNT, jyskePersistentStorage.getCount())
                            .claim(JwtKeys.PWD, jyskePersistentStorage.getPincode())
                            .build();

            final KeyPair keyPair = jyskePersistentStorage.getKeyPair();

            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            JWSSigner signer = new RSASSASigner(keyPair.getPrivate());
            signedJWT.sign(signer);

            final JweRequest jweRequest =
                    new JweRequest(signedJWT.serialize(), JwtValues.LOGIN_TYPE);

            return createJWE(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128CBC_HS256, jweRequest);
        } catch (JOSEException | IllegalStateException e) {
            if (e.getMessage().contains("Could not deserialize object")) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
            throw new IllegalStateException("Couldn't sign JWT object", e);
        }
    }

    private String generateCodeVerifier() {
        return randomValueGenerator.generateRandomBase64UrlEncoded(96).substring(0, 128);
    }

    private String generateCodeChallenge(String codeVerifier) {
        final byte[] digest = Hash.sha256(codeVerifier);
        return EncodingUtils.encodeAsBase64UrlSafe(digest);
    }
}
