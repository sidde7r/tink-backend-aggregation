package se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.filter;

import java.net.URI;
import java.security.KeyPair;
import java.security.PrivateKey;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.BunqBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.BunqBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.rpc.TokenEntity;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BunqSignatureHeaderFilter extends Filter {

    private final SessionStorage sessionStorage;
    private final TemporaryStorage temporaryStorage;
    private final String userAgent;

    public BunqSignatureHeaderFilter(
            SessionStorage sessionStorage, TemporaryStorage temporaryStorage, String userAgent) {
        this.sessionStorage = sessionStorage;
        this.temporaryStorage = temporaryStorage;
        this.userAgent = userAgent;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        if (!httpRequest.getURI().getPath().contains("installation")) {
            addSignatureHeader(httpRequest);
        }
        return nextFilter(httpRequest);
    }

    private void addSignatureHeader(HttpRequest httpRequest) {

        MultivaluedMap<String, Object> requestHeaders = httpRequest.getHeaders();

        String rawHeader =
                httpRequest.getMethod()
                        + " "
                        + getPathAndQuery(httpRequest)
                        + "\n"
                        + toSignatureFormat(BunqBaseConstants.Headers.CACHE_CONTROL, requestHeaders)
                        + toSignatureFormat(
                                BunqBaseConstants.Headers.USER_AGENT.getKey(), userAgent)
                        + toSignatureFormat(BunqBaseConstants.Headers.CLIENT_AUTH, requestHeaders)
                        + toSignatureFormat(BunqBaseConstants.Headers.REQUEST_ID, requestHeaders)
                        + toSignatureFormat(BunqBaseConstants.Headers.GEOLOCATION, requestHeaders)
                        + toSignatureFormat(BunqBaseConstants.Headers.LANGUAGE, requestHeaders)
                        + toSignatureFormat(BunqBaseConstants.Headers.REGION, requestHeaders)
                        + "\n";

        if (httpRequest.getBody() != null) {
            rawHeader += SerializationUtils.serializeToString(httpRequest.getBody());
        }

        httpRequest
                .getHeaders()
                .putSingle(
                        BunqBaseConstants.Headers.CLIENT_SIGNATURE.getKey(),
                        getEncodedSignature(rawHeader));
    }

    private String getEncodedSignature(String rawHeader) {
        PrivateKey privateKey = getPrivateKey();

        byte[] signatureHeader = RSA.signSha256(privateKey, rawHeader.getBytes());
        return EncodingUtils.encodeAsBase64String(signatureHeader);
    }

    // This is needed due to Bunq's header signing requirement, see https://doc.bunq.com/#/signing
    // depending if we are making calls as a PSD2Provider or on behalf of the user we have to use
    // different keys to sign the "X-Bunq-Client-Signature" header so when we update the client
    // authentication token, we need to update which key should be use to sign the client signature
    // header. Depending on which role we are making the calls as and also on which phase of
    // the authentication flow we are we should use different client authentication tokens,
    // i.e. to register a device and start a session we have to use the token that we got from the
    // installation call while any call done after a session is started should use the token
    // received from in the session response as the client authentication token.
    private PrivateKey getPrivateKey() {
        String relevantClientAuthToken =
                sessionStorage
                        .get(StorageKeys.CLIENT_AUTH_TOKEN, TokenEntity.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No valid client auth token found when trying to retrieve installation key pair."))
                        .getToken();
        KeyPair keyPair =
                SerializationUtils.deserializeKeyPair(
                        temporaryStorage.get(relevantClientAuthToken));
        return keyPair.getPrivate();
    }

    private String getPathAndQuery(HttpRequest httpRequest) {
        URI uri = httpRequest.getURI();

        String query = uri.getQuery();
        String path = uri.getPath();

        return query != null ? path + "?" + query : path;
    }

    private String toSignatureFormat(
            BunqBaseConstants.Headers headerConstant,
            MultivaluedMap<String, Object> requestHeaders) {
        return toSignatureFormat(
                headerConstant.getKey(), requestHeaders.getFirst(headerConstant.getKey()));
    }

    private String toSignatureFormat(String key, Object value) {
        return key + ": " + value + "\n";
    }
}
