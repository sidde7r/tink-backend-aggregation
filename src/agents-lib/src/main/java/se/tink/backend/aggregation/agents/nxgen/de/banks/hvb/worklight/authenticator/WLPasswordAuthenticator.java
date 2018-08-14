package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.AuthenticityChallengeHandler;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.CertManager;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.CsrJwt;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.InvokeResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.Jwt;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConfig;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities.AuthorizationCsrEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities.AuthorizationEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities.WlDeviceAutoProvisioningRealmEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.rpc.AuthenticateRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.rpc.InitResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.rpc.JSecurityCheckResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED_TYPE;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Forms.ADAPTER;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Forms.ADAPTER_FACADE;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Forms.ADAPTER_SECURITY_SERVICE;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Forms.COMPRESS_RESPONSE;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Forms.J_PASSWORD;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Forms.J_USERNAME;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Forms.PARAMETERS;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Forms.PROCEDURE;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Forms.PROCEDURE_LOGIN;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Forms.PX2;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Forms.REALM;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Forms.REALM_VALUE;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Forms.SECP;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Forms.SECP_VALUE;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Forms.SECP_VALUE_SHORT;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Forms.USERNAME;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Headers.WL_AUTHORIZATION_IN_BODY;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Headers.X_WL_APP_VERSION;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Storage.WL_INSTANCE_ID;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Url.AUTHENTICATE;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Url.INIT;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Url.INVOKE;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Url.J_SECURITY_CHECK;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.Url.LOGIN;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants.WL_APP_VERSION;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLUtils.encasedJsonToEntity;
import static se.tink.libraries.serialization.utils.SerializationUtils.serializeToString;

/**
 * The aim of this class is to encapsulate the authentication procedure that the agent needs to perform in order to
 * begin querying information from a server powered by the IBM® Worklight® security framework.
 */
public final class WLPasswordAuthenticator implements PasswordAuthenticator {
    private static final Logger logger = LoggerFactory.getLogger(WLPasswordAuthenticator.class);

    private final WLApiClient client;
    private final WLConfig config;
    private final WLAuthenticatorStorage storage;

    public WLPasswordAuthenticator(
            final WLApiClient client,
            final WLAuthenticatorStorage storage,
            final WLConfig config) {
        this.client = client;
        this.config = config;
        this.storage = storage;
    }

    public void authenticate(final String username, final String password)
            throws AuthenticationException, AuthorizationException {
        // 1
        final InitResponse init1Response = init1();
        final String wlInstanceId = init1Response.getWlInstanceId();

        // 2
        init2(init1Response.getWlChallengeData(), wlInstanceId);

        final KeyPair keyPair = getKeyPair();

        // 3
        final String certificate = init3(keyPair, init1Response.getToken(), wlInstanceId);

        // 4
        authenticateStep((RSAPrivateKey) keyPair.getPrivate(), certificate, init1Response.getToken(), wlInstanceId);

        // 5
        login(init1Response.getWlInstanceId());

        // 6
        final String px2 = loginSecurityCheck(username, password, wlInstanceId);

        // 7
        invoke(username, px2, wlInstanceId);

        // 8
        login(wlInstanceId);

        storage.setWlInstanceId(wlInstanceId);
    }

    private KeyPair getKeyPair() {
        return storage.getKeyPair()
                .orElseGet(() -> {
                    final KeyPair keyPair = CertManager.generateKeyPair();
                    storage.setKeyPair(keyPair);
                    return keyPair;
                });
    }

    private RequestBuilder getRequest(final String resource, final MediaType mediaType) {
        return client.getClient().request(config.getEndpointUrl() + resource)
                .header(HttpHeaders.CONTENT_TYPE, mediaType)
                .header(X_WL_APP_VERSION, WL_APP_VERSION);
    }

    private InitResponse init(final RequestBuilder requestBuilder) {
        try {
            requestBuilder.post(HttpResponse.class);
            // Not expecting status 200; throw exception
            throw new IllegalStateException("Expected status 401; received 200");
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 401) { // This is expected according to WL protocol
                return encasedJsonToEntity(e.getResponse(), InitResponse.class);
            } else {
                throw e;
            }
        }
    }

    /**
     * @return E.g. "/apps/services/api/MyModule/iphone/<slug>"
     */
    private String getApiPath(final String slug) {
        return WLConstants.Url.API_ROOT + config.getModuleName() + slug;
    }

    /**
     * Fetch required header, token, challenge data
     */
    private InitResponse init1() {
        final InitResponse initResponse = init(getRequest(getApiPath(INIT), APPLICATION_FORM_URLENCODED_TYPE));

        Preconditions.checkNotNull(initResponse.getWlInstanceId());
        Preconditions.checkNotNull(initResponse.getToken());
        Preconditions.checkNotNull(initResponse.getWlChallengeData());

        return initResponse;
    }

    /**
     * Compute authenticity realm string
     * Submit solution to challenge by sending computed authenticity realm string in Authorization header
     * Assert allowed=true in response
     */
    private void init2(final String wlChallengeData, final String wlInstanceId) {
        final String challengeResponse = AuthenticityChallengeHandler
                .challengeToAuthenticityRealmString(wlChallengeData, config.getModuleName(), config.getAppId());

        final AuthorizationEntity authorizationAuthenticity = new AuthorizationEntity(challengeResponse);
        final InitResponse initResponse = init(
                getRequest(getApiPath(INIT), APPLICATION_FORM_URLENCODED_TYPE)
                        .header(WL_INSTANCE_ID, wlInstanceId)
                        .header(HttpHeaders.AUTHORIZATION, serializeToString(authorizationAuthenticity))
        );

        Preconditions.checkArgument(initResponse.getAllowed(), "Received allowed=false; expected allowed=true");
    }

    /**
     * Initiate Diffie-Hellman key exchange:
     * Send token + client public key in CSR JWT (signed with client private key) in Authorization header
     * Fetch X.509 certificate (server-side public key)
     */
    private String init3(final KeyPair keyPair, final String token, final String wlInstanceId) {
        final Jwt jwt = new CsrJwt(token, (RSAPublicKey) keyPair.getPublic(), config.getModuleName());

        final WlDeviceAutoProvisioningRealmEntity realmEntity = new WlDeviceAutoProvisioningRealmEntity();
        realmEntity.setCsr(CertManager.createJwt(jwt, (RSAPrivateKey) keyPair.getPrivate()));
        final AuthorizationCsrEntity authorizationJwt = new AuthorizationCsrEntity(realmEntity);

        final InitResponse initResponse = init(
                getRequest(getApiPath(INIT), APPLICATION_FORM_URLENCODED_TYPE)
                        .header(WL_INSTANCE_ID, wlInstanceId)
                        .header(HttpHeaders.AUTHORIZATION, serializeToString(authorizationJwt))
        );

        return initResponse.getCertificate();
    }

    /**
     * Send back X.509 certificate + token in JWT (signed with private key)
     */
    private void authenticateStep(
            final RSAPrivateKey privateKey,
            final String certificate,
            final String token,
            final String wlInstanceId) {
        final AuthenticateRequest body = new AuthenticateRequest(token, certificate, privateKey,
                config.getModuleName());
        getRequest(getApiPath(AUTHENTICATE), MediaType.APPLICATION_JSON_TYPE)
                .header(WL_INSTANCE_ID, wlInstanceId)
                .header(HttpHeaders.AUTHORIZATION, WL_AUTHORIZATION_IN_BODY)
                .body(body)
                .post(HttpResponse.class);
    }

    /**
     * Tell server that we want to log in
     */
    private void login(final String wlInstanceId) {
        final Form form = new Form.Builder()
                .put(REALM, REALM_VALUE)
                .build();
        final HttpResponse response = getRequest(getApiPath(LOGIN), APPLICATION_FORM_URLENCODED_TYPE)
                .header(WL_INSTANCE_ID, wlInstanceId)
                .body(form.serialize())
                .post(HttpResponse.class);
    }

    /**
     * Send username + password
     * Fetch px2 string
     */
    private String loginSecurityCheck(String username, String password, final String wlInstanceId) {
        final Form form = new Form.Builder()
                .put(J_USERNAME)
                .put(J_PASSWORD)
                .put(USERNAME, username)
                .put(PX2, password)
                .put(SECP, SECP_VALUE)
                .build();
        final HttpResponse httpResponse = getRequest(
                J_SECURITY_CHECK,
                APPLICATION_FORM_URLENCODED_TYPE)
                .header(WL_INSTANCE_ID, wlInstanceId)
                .body(form.serialize())
                .post(HttpResponse.class);

        final JSecurityCheckResponse response = encasedJsonToEntity(httpResponse, JSecurityCheckResponse.class);

        Preconditions.checkNotNull(response.getPx2());

        return response.getPx2();
    }

    /**
     * Send username + px2 string
     * Assert isSuccessful, legitimation, authentication/authorization error codes
     */
    private void invoke(final String username, final String px2, final String wlInstanceId)
            throws LoginException, AuthorizationException {
        final Form request = new Form.Builder()
                .put(ADAPTER, ADAPTER_SECURITY_SERVICE)
                .put(PROCEDURE, PROCEDURE_LOGIN)
                .put(COMPRESS_RESPONSE)
                .put(PARAMETERS, serializeToString(ImmutableList.of(username, px2, SECP_VALUE_SHORT)))
                .build();
        final HttpResponse httpResponse = getRequest(INVOKE, APPLICATION_FORM_URLENCODED_TYPE)
                .header(WL_INSTANCE_ID, wlInstanceId)
                .body(request.serialize())
                .post(HttpResponse.class);

        final InvokeResponse response = encasedJsonToEntity(httpResponse, InvokeResponse.class);

        response.getMessages().ifPresent(logger::warn);

        if (response.isPasswordIncorrect()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        } else if (response.isAccountLocked()) {
            throw AuthorizationError.ACCOUNT_BLOCKED.exception();
        } else if (!response.getIsSuccessful()) {
            throw new IllegalStateException("Failed on /invoke/: expected isSuccessful to be true");
        } else if (!response.isLegit()) {
            throw new IllegalStateException("Failed on /invoke/: expected legitimation to be \"1\"");
        }
    }
}
