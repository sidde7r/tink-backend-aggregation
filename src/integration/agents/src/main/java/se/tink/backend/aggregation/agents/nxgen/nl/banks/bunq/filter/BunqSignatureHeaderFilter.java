package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.filter;

import java.net.URI;
import java.security.KeyPair;
import java.security.PrivateKey;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.BunqConstants;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BunqSignatureHeaderFilter extends Filter {

    private final PersistentStorage persistentStorage;
    private final String userAgent;

    public BunqSignatureHeaderFilter(PersistentStorage persistentStorage, String userAgent) {
        this.persistentStorage = persistentStorage;
        this.userAgent = userAgent;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws HttpClientException, HttpResponseException {
        addSignatureHeader(httpRequest);
        return nextFilter(httpRequest);
    }

    private void addSignatureHeader(HttpRequest httpRequest) {

        MultivaluedMap<String, Object> requestHeaders = httpRequest.getHeaders();

        String rawHeader = httpRequest.getMethod() + " " + getPathAndQuery(httpRequest) + "\n"
                + toSignatureFormat(BunqConstants.Headers.CACHE_CONTROL, requestHeaders)
                + toSignatureFormat(BunqConstants.Headers.USER_AGENT.getKey(), userAgent)
                + toSignatureFormat(BunqConstants.Headers.CLIENT_AUTH, requestHeaders)
                + toSignatureFormat(BunqConstants.Headers.REQUEST_ID, requestHeaders)
                + toSignatureFormat(BunqConstants.Headers.GEOLOCATION, requestHeaders)
                + toSignatureFormat(BunqConstants.Headers.LANGUAGE, requestHeaders)
                + toSignatureFormat(BunqConstants.Headers.REGION, requestHeaders)
                + "\n";
        
        if (httpRequest.getBody() != null) {
            rawHeader += SerializationUtils.serializeToString(httpRequest.getBody());
        }

        httpRequest.getHeaders().putSingle(BunqConstants.Headers.CLIENT_SIGNATURE.getKey(),
                getEncodedSignature(rawHeader));
    }

    private String getEncodedSignature(String rawHeader) {
        PrivateKey privateKey = getPrivateKey();

        byte[] signatureHeader = RSA.signSha256(privateKey, rawHeader.getBytes());
        return EncodingUtils.encodeAsBase64String(signatureHeader);
    }

    private PrivateKey getPrivateKey() {
        KeyPair keyPair = SerializationUtils.deserializeKeyPair(
                persistentStorage.get(BunqConstants.StorageKeys.DEVICE_RSA_SIGNING_KEY_PAIR));
        return keyPair.getPrivate();
    }

    private String getPathAndQuery(HttpRequest httpRequest) {
        URI uri = httpRequest.getURI();

        String query = uri.getQuery();
        String path = uri.getPath();

        return query != null ? path + "?" + query : path;
    }

    private String toSignatureFormat(BunqConstants.Headers headerConstant,
            MultivaluedMap<String, Object> requestHeaders) {
        return toSignatureFormat(headerConstant.getKey(), requestHeaders.getFirst(headerConstant.getKey()));
    }

    private String toSignatureFormat(String key, Object value) {
        return key + ": " + value + "\n";
    }
}
