package se.tink.backend.aggregation.nxgen.http.client;

import com.fasterxml.jackson.databind.Module;
import com.sun.jersey.api.client.Client;
import java.security.KeyStore;
import java.util.Date;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import org.apache.http.cookie.Cookie;
import se.tink.backend.aggregation.agents.utils.jersey.interceptor.MessageSignInterceptor;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.Filterable;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.handler.HttpResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.redirect.handler.RedirectHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.serializecontainer.SerializeContainer;
import se.tink.backend.aggregation.nxgen.http.truststrategy.TrustAllCertificatesStrategy;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface TinkHttpClient extends Filterable<TinkHttpClient> {

    void setMessageSignInterceptor(MessageSignInterceptor messageSignInterceptor);

    String getUserAgent();

    SSLContext getSslContext();

    String getHeaderAggregatorIdentifier();

    void setResponseStatusHandler(HttpResponseStatusHandler responseStatusHandler);

    Client getInternalClient();

    // +++ Configuration +++
    void addMessageReader(MessageBodyReader<?> messageBodyReader);

    void addMessageWriter(MessageBodyWriter<?> messageBodyWriter);

    void registerJacksonModule(Module module);

    /**
     * @param cipherSuites A list of cipher suites to be presented to the server at TLS Client Hello
     *     in order of preference, e.g. TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384 etc. This might be
     *     necessary if the choice of cipher suite causes the TLS handshake to fail.
     */
    void setCipherSuites(final List<String> cipherSuites);

    void setUserAgent(String userAgent);

    void setCookieSpec(String cookieSpec);

    void disableSignatureRequestHeader();

    void disableAggregatorHeader();

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

    void setEidasProxy(EidasProxyConfiguration conf);

    /**
     * @deprecated This should not be used. Use `setEidasProxy` if making proxied requests. Use
     *     `QsealcSigner` if requesting signatures
     */
    @Deprecated
    void setEidasSign(EidasProxyConfiguration conf);

    void addRedirectHandler(RedirectHandler handler);

    void setDebugOutput(boolean debugOutput);

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
