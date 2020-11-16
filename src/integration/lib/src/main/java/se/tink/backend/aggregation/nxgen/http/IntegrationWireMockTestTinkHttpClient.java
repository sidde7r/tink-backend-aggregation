package se.tink.backend.aggregation.nxgen.http;

import com.fasterxml.jackson.databind.Module;
import com.sun.jersey.api.client.Client;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.SSLContext;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import se.tink.backend.aggregation.agents.utils.jersey.interceptor.MessageSignInterceptor;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.http.client.LoggingStrategy;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.handler.HttpResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.redirect.handler.RedirectHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.serializecontainer.SerializeContainer;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class IntegrationWireMockTestTinkHttpClient implements TinkHttpClient {

    private final TinkHttpClient tinkHttpClient;
    private final String wireMockServerHost;

    public IntegrationWireMockTestTinkHttpClient(
            TinkHttpClient tinkHttpClient, final String wireMockServerHost) {
        this.tinkHttpClient = tinkHttpClient;
        this.wireMockServerHost = wireMockServerHost;
        this.tinkHttpClient.addRedirectHandler(
                new RedirectHandler() {
                    @Override
                    public String modifyRedirectUri(String uri) {
                        try {
                            URI currentUri = new URI(uri);
                            URL fixedUrl = toWireMockHost(currentUri);
                            return fixedUrl.toString();
                        } catch (URISyntaxException e) {
                            throw new IllegalArgumentException(e);
                        }
                    }
                });
    }

    @Override
    public void setMessageSignInterceptor(MessageSignInterceptor messageSignInterceptor) {
        tinkHttpClient.setMessageSignInterceptor(messageSignInterceptor);
    }

    @Override
    public String getUserAgent() {
        return tinkHttpClient.getUserAgent();
    }

    @Override
    public SSLContext getSslContext() {
        return tinkHttpClient.getSslContext();
    }

    @Override
    public String getHeaderAggregatorIdentifier() {
        return tinkHttpClient.getHeaderAggregatorIdentifier();
    }

    @Override
    public void setResponseStatusHandler(HttpResponseStatusHandler responseStatusHandler) {
        tinkHttpClient.setResponseStatusHandler(responseStatusHandler);
    }

    @Override
    public Client getInternalClient() {
        return tinkHttpClient.getInternalClient();
    }

    @Override
    public void addMessageReader(MessageBodyReader<?> messageBodyReader) {
        tinkHttpClient.addMessageReader(messageBodyReader);
    }

    @Override
    public void addMessageWriter(MessageBodyWriter<?> messageBodyWriter) {
        tinkHttpClient.addMessageWriter(messageBodyWriter);
    }

    @Override
    public void registerJacksonModule(Module module) {
        tinkHttpClient.registerJacksonModule(module);
    }

    @Override
    public void setCipherSuites(List<String> cipherSuites) {
        tinkHttpClient.setCipherSuites(cipherSuites);
    }

    @Override
    public void setUserAgent(String userAgent) {
        tinkHttpClient.setUserAgent(userAgent);
    }

    @Override
    public void setCookieSpec(String cookieSpec) {
        tinkHttpClient.setCookieSpec(cookieSpec);
    }

    @Override
    public void disableSignatureRequestHeader() {
        tinkHttpClient.disableSignatureRequestHeader();
    }

    @Override
    public void disableAggregatorHeader() {
        tinkHttpClient.disableAggregatorHeader();
    }

    @Override
    public void setLoggingStrategy(LoggingStrategy loggingStrategy) {
        tinkHttpClient.setLoggingStrategy(loggingStrategy);
    }

    @Override
    public void setEidasProxyConfiguration(EidasProxyConfiguration eidasProxyConfiguration) {
        tinkHttpClient.setEidasProxyConfiguration(eidasProxyConfiguration);
    }

    @Override
    public void setTimeout(int milliseconds) {
        tinkHttpClient.setTimeout(milliseconds);
    }

    @Override
    public void setChunkedEncoding(boolean chunkedEncoding) {
        tinkHttpClient.setChunkedEncoding(chunkedEncoding);
    }

    @Override
    public void setMaxRedirects(int maxRedirects) {
        tinkHttpClient.setMaxRedirects(maxRedirects);
    }

    @Override
    public void setFollowRedirects(boolean followRedirects) {
        tinkHttpClient.setFollowRedirects(followRedirects);
    }

    @Override
    public void disableSslVerification() {
        tinkHttpClient.disableSslVerification();
    }

    @Override
    public void loadTrustMaterial(KeyStore truststore, TrustStrategy trustStrategy) {
        tinkHttpClient.loadTrustMaterial(truststore, trustStrategy);
    }

    @Override
    public void setSslProtocol(String sslProtocol) {
        tinkHttpClient.setSslProtocol(sslProtocol);
    }

    @Override
    public void setSslClientCertificate(byte[] clientCertificateBytes, String password) {
        // NOOP
    }

    @Override
    public void trustRootCaCertificate(byte[] jksData, String password) {
        // NOOP
    }

    @Override
    public void setDebugProxy(String uri) {
        tinkHttpClient.setDebugProxy(uri);
    }

    @Override
    public void setProductionProxy(String uri, String username, String password) {
        tinkHttpClient.setProductionProxy(uri, username, password);
    }

    @Override
    public void setEidasIdentity(EidasIdentity eidasIdentity) {
        // NOOP
    }

    @Override
    public void setEidasProxy(EidasProxyConfiguration conf) {
        // NOOP
    }

    @Override
    @Deprecated
    public void setEidasSign(EidasProxyConfiguration conf) {
        tinkHttpClient.setEidasSign(conf);
    }

    @Override
    public void addRedirectHandler(RedirectHandler handler) {
        tinkHttpClient.addRedirectHandler(handler);
    }

    @Override
    public void setDebugOutput(boolean debugOutput) {
        tinkHttpClient.setDebugOutput(debugOutput);
    }

    @Override
    public List<Cookie> getCookies() {
        return tinkHttpClient.getCookies();
    }

    @Override
    public void addCookie(Cookie... cookies) {
        tinkHttpClient.addCookie(cookies);
    }

    @Override
    public void clearCookies() {
        tinkHttpClient.clearCookies();
    }

    @Override
    public void clearExpiredCookies(Date date) {
        tinkHttpClient.clearExpiredCookies(date);
    }

    @Override
    public void clearExpiredCookies() {
        tinkHttpClient.clearExpiredCookies();
    }

    @Override
    public void addPersistentHeader(String key, String value) {
        tinkHttpClient.addPersistentHeader(key, value);
    }

    @Override
    public void clearPersistentHeaders() {
        tinkHttpClient.clearPersistentHeaders();
    }

    @Override
    public boolean isPersistentHeaderPresent(String headerKey) {
        return tinkHttpClient.isPersistentHeaderPresent(headerKey);
    }

    @Override
    public String serialize() {
        return tinkHttpClient.serialize();
    }

    @Override
    public void initialize(SerializeContainer serializeContainer) {
        tinkHttpClient.initialize(serializeContainer);
    }

    @Override
    public RequestBuilder request(String url) {
        return request(new URL(url));
    }

    public RequestBuilder request(URL url) {
        try {
            return new IntegrationWireMockTestRequestBuilder(
                    tinkHttpClient.request(toWireMockHost(new URI(url.toString()))));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public <T> T request(Class<T> c, HttpRequest request)
            throws HttpClientException, HttpResponseException {

        request.setUrl(toWireMockHost(request.getURI()));
        return tinkHttpClient.request(c, request);
    }

    @Override
    public void request(HttpRequest request) throws HttpClientException, HttpResponseException {
        request.setUrl(toWireMockHost(request.getURI()));
        tinkHttpClient.request(request);
    }

    @Override
    public void cutFilterTail() {
        tinkHttpClient.cutFilterTail();
    }

    @Override
    public TinkHttpClient addFilter(Filter filter) {
        return tinkHttpClient.addFilter(filter);
    }

    @Override
    public TinkHttpClient removeFilter(Filter filter) {
        return tinkHttpClient.removeFilter(filter);
    }

    @Override
    public boolean isFilterPresent(Filter filter) {
        return tinkHttpClient.isFilterPresent(filter);
    }

    private URL toWireMockHost(final URI uri) {

        try {
            URI newUri =
                    new URI(
                            uri.getScheme().toLowerCase(Locale.US),
                            wireMockServerHost,
                            uri.getPath(),
                            uri.getQuery(),
                            uri.getFragment());

            return new URL(newUri.toString());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
