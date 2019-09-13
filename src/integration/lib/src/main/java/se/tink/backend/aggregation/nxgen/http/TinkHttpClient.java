package se.tink.backend.aggregation.nxgen.http;

import com.fasterxml.jackson.databind.Module;
import com.sun.jersey.api.client.Client;
import java.security.KeyStore;
import java.util.Date;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import org.apache.http.cookie.Cookie;
import se.tink.backend.aggregation.agents.utils.jersey.MessageSignInterceptor;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filterable;
import se.tink.backend.aggregation.nxgen.http.redirect.RedirectHandler;
import se.tink.backend.aggregation.nxgen.http.truststrategy.TrustAllCertificatesStrategy;

public interface TinkHttpClient extends Filterable<TinkHttpClient> {

    void setMessageSignInterceptor(MessageSignInterceptor messageSignInterceptor);

    String getUserAgent();

    SSLContext getSslContext();

    String getHeaderAggregatorIdentifier();

    Client getInternalClient();

    // +++ Configuration +++
    void addMessageReader(MessageBodyReader<?> messageBodyReader);

    void addMessageWriter(MessageBodyWriter<?> messageBodyWriter);

    void registerJacksonModule(Module module);

    void setCipherSuites(final List<String> cipherSuites);

    void setUserAgent(String userAgent);

    void setCookieSpec(String cookieSpec);

    void disableSignatureRequestHeader();

    void setEidasProxyConfiguration(EidasProxyConfiguration eidasProxyConfiguration);

    void setTimeout(int milliseconds);

    void setChunkedEncoding(boolean chunkedEncoding);

    void setMaxRedirects(int maxRedirects);

    void setFollowRedirects(boolean followRedirects);

    void disableSslVerification();

    void loadTrustMaterial(KeyStore truststore, TrustAllCertificatesStrategy trustStrategy);

    void setSslProtocol(String sslProtocol);

    void setSslClientCertificate(byte[] clientCertificateBytes, String password);

    void trustRootCaCertificate(byte[] jksData, String password);

    void setDebugProxy(String uri);

    void setProductionProxy(String uri, String username, String password);

    void setEidasIdentity(EidasIdentity eidasIdentity);

    void setEidasProxy(EidasProxyConfiguration conf, String legacyCertId);

    /**
     * @deprecated This should not be used. Use `setEidasProxy` if making proxied requests. Use
     *     `QsealcSigner` if requesting signatures
     */
    @Deprecated
    void setEidasSign(EidasProxyConfiguration conf);

    void addRedirectHandler(RedirectHandler handler);

    void setDebugOutput(boolean debugOutput);

    void setCensorSensitiveHeaders(final boolean censorSensitiveHeadersEnabled);

    // --- Configuration ---

    // +++ Cookies +++
    List<Cookie> getCookies();

    void addCookie(Cookie... cookies);

    void clearCookies();

    void clearExpiredCookies(Date date);

    void clearExpiredCookies();
    // --- Cookies ---

    // +++ Persistent request data +++
    void addPersistentHeader(String key, String value);

    void clearPersistentHeaders();

    boolean isPersistentHeaderPresent(String headerKey);
    // --- Persistent request data ---

    // +++ Serialization +++
    // Serialize/deserialize the following data:
    //  - Cookies
    //  - PersistentHeaders
    String serialize();

    void initialize(SerializeContainer serializeContainer);
    // --- Serialization ---

    // +++ Requests +++
    RequestBuilder request(String url);

    RequestBuilder request(URL url);

    <T> T request(Class<T> c, HttpRequest request)
            throws HttpClientException, HttpResponseException;

    void request(HttpRequest request) throws HttpClientException, HttpResponseException;
    // --- Requests ---
}
