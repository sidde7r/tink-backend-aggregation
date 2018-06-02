package se.tink.backend.aggregation.agents.utils.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

@SuppressWarnings("deprecation")
public class HttpClientHelper {
    private static final int CONNECT_TIMEOUT_MS = 10000;
    private static final int SOCKET_TIMEOUT_MS = 30000;

    public static String DEFAULT_USER_AGENT = "Mozilla/5.0 (Linux; U; Android 2.1; en-us; Nexus One Build/ERD62) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17";

    private String userAgent = DEFAULT_USER_AGENT;
    private DefaultHttpClient httpclient;
    private HttpContext context;
    private String currentURI;
    private boolean acceptInvalidCertificates = false;
    private String charset = HTTP.UTF_8;
    private HashMap<String, String> headers;    
    private PrintStream logOutputStream;

    public HttpClientHelper() {
        this(false);
    }

    public HttpClientHelper(boolean acceptInvalidCertificates) {
        this(acceptInvalidCertificates, false);
    }

    public HttpClientHelper(boolean acceptInvalidCertificates, boolean allowCircularRedirects) {
        this.acceptInvalidCertificates = acceptInvalidCertificates;
        this.headers = new HashMap<String, String>();
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, this.charset);
        params.setBooleanParameter("http.protocol.expect-continue", false);
        HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT_MS);
        HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT_MS);
        if (allowCircularRedirects) {
            params.setBooleanParameter("http.protocol.allow-circular-redirects", true);
        }
        if (acceptInvalidCertificates) {
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            // registry.register(new Scheme("https", new EasySSLSocketFactory(),
            // 443));
            ClientConnectionManager manager = new ThreadSafeClientConnManager(params, registry);
            httpclient = new DefaultHttpClient(manager, params);
        } else {
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
            ClientConnectionManager manager = new ThreadSafeClientConnManager(params, registry);
            httpclient = new DefaultHttpClient(manager, params);
        }

        httpclient.addRequestInterceptor((r, c) -> {
            if (logOutputStream != null) {
                logOutputStream.println("* Client out-bound request");
                logOutputStream.println("> " + r.getRequestLine().toString());
                Header[] requestHeaders = r.getAllHeaders();
                for (Header header : requestHeaders) {
                    logOutputStream.println("> " + header.toString());
                }
            }
        });

        httpclient.addResponseInterceptor((r, c) -> {
            if (logOutputStream != null) {
                logOutputStream.println("* Client in-bound response");
                logOutputStream.println("< " + r.getStatusLine().toString());
                Header[] responseHeaders = r.getAllHeaders();
                for (Header header : responseHeaders) {
                    logOutputStream.println("< " + header.toString());
                }
            }
        });

        context = new BasicHttpContext();
    }

    public String open(String url) {
        return this.open(url, new ArrayList<NameValuePair>());
    }

    public String post(String url) throws ClientProtocolException, IOException {
        return this.open(url, new ArrayList<NameValuePair>(), true);
    }

    public String open(String url, List<NameValuePair> postData) {
        return open(url, postData, false);
    }

    public String open(String url, List<NameValuePair> postData, boolean forcePost) {
        return open(url, postData, forcePost, new BasicResponseHandler());
    }

    public String open(String authenticationUrl, ResponseHandler<String> responseHandler) {
        return open(authenticationUrl, null, false, responseHandler);
    }

    public String open(String url, List<NameValuePair> postData, boolean forcePost,
            ResponseHandler<String> responseHandler) {
        this.currentURI = url;
        String response;
        String[] headerKeys = this.headers.keySet().toArray(new String[headers.size()]);
        String[] headerVals = this.headers.values().toArray(new String[headers.size()]);
        HttpUriRequest request;
        if ((postData == null || postData.isEmpty()) && !forcePost) {
            // URL urli = new URL(url);
            request = new HttpGet(url);
        } else {
            request = new HttpPost(url);
            try {
                ((HttpPost) request).setEntity(new UrlEncodedFormEntity(postData, this.charset));
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
        if (userAgent != null) {
            request.addHeader("User-Agent", userAgent);
        }

        for (int i = 0; i < headerKeys.length; i++) {
            request.addHeader(headerKeys[i], headerVals[i]);
        }

        try {
            response = httpclient.execute(request, responseHandler, context);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        if (logOutputStream != null) {
            logOutputStream.println("<");
            logOutputStream.println(response);
        }

        // HttpUriRequest currentReq =
        // (HttpUriRequest)context.getAttribute(ExecutionContext.HTTP_REQUEST);
        // HttpHost currentHost =
        // (HttpHost)context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
        // this.currentURI = currentHost.toURI() + currentReq.getURI();
        this.currentURI = request.getURI().toString();

        return response;
    }

    public InputStream openStream(String url) throws ClientProtocolException, IOException {
        return openStream(url, new BasicHttpEntity(), false);
    }

    public HttpEntity toEntity(List<NameValuePair> postData) {
        if (postData != null && !postData.isEmpty()) {
            try {
                return new UrlEncodedFormEntity(postData, this.charset);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public InputStream openStream(String url, List<NameValuePair> postData, boolean forcePost)
            throws ClientProtocolException, IOException {
        return openStream(url, toEntity(postData), forcePost);
    }

    public InputStream openStream(String url, String postData, boolean forcePost) throws ClientProtocolException,
            IOException {
        return openStream(url, postData != null ? new StringEntity(postData, this.charset) : null, forcePost);
    }

    public InputStream openStream(String url, HttpEntity postData, boolean forcePost) throws ClientProtocolException,
            IOException {
        this.currentURI = url;
        String[] headerKeys = this.headers.keySet().toArray(new String[headers.size()]);
        String[] headerVals = this.headers.values().toArray(new String[headers.size()]);
        HttpUriRequest request;
        if (postData == null && !forcePost) {
            request = new HttpGet(url);
        } else {
            request = new HttpPost(url);
            ((HttpPost) request).setEntity(postData);
        }
        if (userAgent != null) {
            request.addHeader("User-Agent", userAgent);
        }

        for (int i = 0; i < headerKeys.length; i++) {
            request.addHeader(headerKeys[i], headerVals[i]);
        }
        this.currentURI = request.getURI().toString();
        HttpResponse response = httpclient.execute(request);
        HttpEntity entity = response.getEntity();
        return entity.getContent();
    }

    private static class NoRedirectStrategy implements RedirectStrategy {

        @Override
        public HttpUriRequest getRedirect(HttpRequest arg0, HttpResponse arg1, HttpContext arg2)
                throws ProtocolException {
            // Not needed to be implemented.
            return null;
        }

        @Override
        public boolean isRedirected(HttpRequest arg0, HttpResponse arg1, HttpContext arg2) throws ProtocolException {
            return false;
        }

    }

    public void setFollowRedirect(boolean followRedirect) {
        if (followRedirect) {
            httpclient.setRedirectStrategy(DefaultRedirectStrategy.INSTANCE);
        } else {
            httpclient.setRedirectStrategy(new NoRedirectStrategy());
        }
    }

    public void setLogOutputStream(OutputStream logOutputStream) throws UnsupportedEncodingException {
        this.logOutputStream = new PrintStream(logOutputStream, true, "UTF-8");
    }

    public void close() {
        httpclient.getConnectionManager().shutdown();

        if (logOutputStream != null) {
            logOutputStream.close();
        }
    }

    public HttpContext getContext() {
        return context;
    }

    public String getCurrentURI() {
        return currentURI;
    }

    public DefaultHttpClient getHttpclient() {
        return httpclient;
    }

    public void setContentCharset(String charset) {
        this.charset = charset;
        HttpProtocolParams.setContentCharset(httpclient.getParams(), this.charset);
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public void setKeepAliveTimeout(final int seconds) {
        httpclient.setKeepAliveStrategy((response, arg1) -> seconds);
    }

    public String removeHeader(String key) {
        return this.headers.remove(key);
    }

    public void clearHeaders() {
        this.headers.clear();
    }

    public HashMap<String, String> getRequestHeaders() {
        return this.headers;
    }

    public boolean acceptsInvalidCertificates() {
        return acceptInvalidCertificates;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
