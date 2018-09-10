package se.tink.libraries.net;
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

import java.net.URI;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import tink.org.apache.http.HttpHost;
import tink.org.apache.http.auth.AuthScope;
import tink.org.apache.http.auth.UsernamePasswordCredentials;
import tink.org.apache.http.client.CookieStore;
import tink.org.apache.http.client.CredentialsProvider;
import tink.org.apache.http.client.params.ClientPNames;
import tink.org.apache.http.client.params.CookiePolicy;
import tink.org.apache.http.conn.ClientConnectionManager;
import tink.org.apache.http.conn.params.ConnRoutePNames;
import tink.org.apache.http.impl.client.BasicCookieStore;
import tink.org.apache.http.impl.client.DefaultHttpClient;
import tink.org.apache.http.params.HttpParams;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;

/**
 * A {@link Client} that utilizes the Apache HTTP Client to send and receive
 * HTTP request and responses.
 * <p>
 * The following properties are only supported at construction of this class:
 * {@link com.sun.jersey.client.apache4.config.ApacheHttpClient4Config#PROPERTY_CONNECTION_MANAGER}<br>
 * {@link com.sun.jersey.client.apache4.config.ApacheHttpClient4Config#PROPERTY_HTTP_PARAMS}}<br>
 * {@link com.sun.jersey.client.apache4.config.ApacheHttpClient4Config#PROPERTY_CREDENTIALS_PROVIDER}}<br>
 * {@link com.sun.jersey.client.apache4.config.ApacheHttpClient4Config#PROPERTY_DISABLE_COOKIES}}<br>
 * {@link com.sun.jersey.client.apache4.config.ApacheHttpClient4Config#PROPERTY_PROXY_URI}}<br>
 * {@link com.sun.jersey.client.apache4.config.ApacheHttpClient4Config#PROPERTY_PROXY_USERNAME}}<br>
 * {@link com.sun.jersey.client.apache4.config.ApacheHttpClient4Config#PROPERTY_PROXY_PASSWORD}}<br>
 * {@link com.sun.jersey.client.apache4.config.ApacheHttpClient4Config#PROPERTY_PREEMPTIVE_BASIC_AUTHENTICATION}}<br>
 * <p>
 * By default a request entity is buffered and repeatable such that
 * authorization may be performed automatically in response to a 401 response.
 * <p>
 * If the property {@link ClientConfig#PROPERTY_CHUNKED_ENCODING_SIZE} size
 * is set to a value greater than 0 then chunked encoding will be enabled
 * and the request entity (if present) will not be buffered and is not
 * repeatable. For authorization to work in such scenarios the property
 * {@link com.sun.jersey.client.apache4.config.ApacheHttpClient4Config#PROPERTY_PREEMPTIVE_BASIC_AUTHENTICATION} must
 * be set to true.
 * <p>
 * If a {@link com.sun.jersey.api.client.ClientResponse} is obtained and an
 * entity is not read from the response then
 * {@link com.sun.jersey.api.client.ClientResponse#close() } MUST be called 
 * after processing the response to release connection-based resources.
 *
 * @see ApacheHttpClient4Config#PROPERTY_CONNECTION_MANAGER
 *
 * @author jorgeluisw@mac.com
 * @author Paul.Sandoz@Sun.Com
 * @author pavel.bucek@oracle.com
 */
public class TinkApacheHttpClient4 extends Client {

    private final TinkApacheHttpClient4Handler client4Handler;

    /**
     * Create a new client instance.
     *
     */
    public TinkApacheHttpClient4() {
        this(createDefaultClientHandler(null), new DefaultClientConfig(), null);
    }

    /**
     * Create a new client instance.
     *
     * @param root the root client handler for dispatching a request and
     *        returning a response.
     */
    public TinkApacheHttpClient4(final TinkApacheHttpClient4Handler root) {
        this(root, new DefaultClientConfig(), null);
    }

    /**
     * Create a new client instance with a client configuration.
     *
     * @param root the root client handler for dispatching a request and
     *        returning a response.
     * @param config the client configuration.
     */
    public TinkApacheHttpClient4(final TinkApacheHttpClient4Handler root, final ClientConfig config) {
        this(root, config, null);
    }

    /**
     * Create a new instance with a client configuration and a
     * component provider.
     *
     * @param root the root client handler for dispatching a request and
     *        returning a response.
     * @param config the client configuration.
     * @param provider the IoC component provider factory.
     */
    public TinkApacheHttpClient4(final TinkApacheHttpClient4Handler root, final ClientConfig config,
                             final IoCComponentProviderFactory provider) {
        super(root, config, provider);

        this.client4Handler = root;
    }

    /**
     * Get the Apache HTTP client handler.
     *
     * @return the Apache HTTP client handler.
     */
    public TinkApacheHttpClient4Handler getClientHandler() {
        return client4Handler;
    }

    /**
     * Create a default client.
     *
     * @return a default client.
     */
    public static TinkApacheHttpClient4 create() {
        return new TinkApacheHttpClient4(createDefaultClientHandler(null));
    }

    /**
     * Create a default client with client configuration.
     *
     * @param cc the client configuration.
     * @return a default client.
     */
    public static TinkApacheHttpClient4 create(final ClientConfig cc) {
        return new TinkApacheHttpClient4(createDefaultClientHandler(cc), cc);
    }

    /**
     * Create a default client with client configuration and component provider.
     *
     * @param cc the client configuration.
     * @param provider the IoC component provider factory.
     * @return a default client.
     */
    public static TinkApacheHttpClient4 create(final ClientConfig cc, final IoCComponentProviderFactory provider) {
        return new TinkApacheHttpClient4(createDefaultClientHandler(cc), cc, provider);
    }

    /**
     * Create a default Apache HTTP client handler.
     *
     * @param cc ClientConfig instance. Might be null.
     *
     * @return a default Apache HTTP client handler.
     */
    private static TinkApacheHttpClient4Handler createDefaultClientHandler(final ClientConfig cc) {

        Object connectionManager = null;
        Object httpParams = null;

        if(cc != null) {
            connectionManager = cc.getProperties().get(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER);
            if(connectionManager != null) {
                if(!(connectionManager instanceof ClientConnectionManager)) {
                    Logger.getLogger(TinkApacheHttpClient4.class.getName()).log(
                            Level.WARNING,
                            "Ignoring value of property " + ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER +
                                    " (" + connectionManager.getClass().getName() +
                                    ") - not instance of org.apache.http.conn.ClientConnectionManager."
                    );
                    connectionManager = null;
                }
            }

            httpParams = cc.getProperties().get(ApacheHttpClient4Config.PROPERTY_HTTP_PARAMS);
            if(httpParams != null) {
                if(!(httpParams instanceof HttpParams)) {
                    Logger.getLogger(TinkApacheHttpClient4.class.getName()).log(
                            Level.WARNING,
                            "Ignoring value of property " + ApacheHttpClient4Config.PROPERTY_HTTP_PARAMS +
                                    " (" + httpParams.getClass().getName() +
                                    ") - not instance of org.apache.http.params.HttpParams."
                    );
                    httpParams = null;
                }
            }
        }


        final DefaultHttpClient client = new DefaultHttpClient(
                (ClientConnectionManager)connectionManager,
                (HttpParams)httpParams
        );

        CookieStore cookieStore = null;
        boolean preemptiveBasicAuth = false;

        if(cc != null) {
            for (Map.Entry<String, Object> entry : cc.getProperties().entrySet()) {
                client.getParams().setParameter(entry.getKey(), entry.getValue());
            }

            if (cc.getPropertyAsFeature(ApacheHttpClient4Config.PROPERTY_DISABLE_COOKIES)) {
                client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
            }

            Object credentialsProvider = cc.getProperty(ApacheHttpClient4Config.PROPERTY_CREDENTIALS_PROVIDER);
            if(credentialsProvider != null && (credentialsProvider instanceof CredentialsProvider)) {
                client.setCredentialsProvider((CredentialsProvider)credentialsProvider);
            }

            final Object proxyUri = cc.getProperties().get(ApacheHttpClient4Config.PROPERTY_PROXY_URI);
            if(proxyUri != null) {
                final URI u = getProxyUri(proxyUri);
                final HttpHost proxy = new HttpHost(u.getHost(), u.getPort(), u.getScheme());

                if(cc.getProperties().containsKey(ApacheHttpClient4Config.PROPERTY_PROXY_USERNAME) &&
                        cc.getProperties().containsKey(ApacheHttpClient4Config.PROPERTY_PROXY_PASSWORD)) {

                    client.getCredentialsProvider().setCredentials(
                            new AuthScope(u.getHost(), u.getPort()),
                            new UsernamePasswordCredentials(
                                    cc.getProperty(ApacheHttpClient4Config.PROPERTY_PROXY_USERNAME).toString(),
                                    cc.getProperty(ApacheHttpClient4Config.PROPERTY_PROXY_PASSWORD).toString())
                    );

                }
                client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            }

            preemptiveBasicAuth = cc.getPropertyAsFeature(ApacheHttpClient4Config.PROPERTY_PREEMPTIVE_BASIC_AUTHENTICATION);
        }

        if(client.getParams().getParameter(ClientPNames.COOKIE_POLICY) == null || !client.getParams().getParameter(ClientPNames.COOKIE_POLICY).equals(CookiePolicy.IGNORE_COOKIES)) {
            cookieStore = new BasicCookieStore();
            client.setCookieStore(cookieStore);
        }

        return new TinkApacheHttpClient4Handler(client, cookieStore, preemptiveBasicAuth);
    }

    private static URI getProxyUri(final Object proxy) {
        if (proxy instanceof URI) {
            return (URI) proxy;
        } else if (proxy instanceof String) {
            return URI.create((String) proxy);
        } else {
            throw new ClientHandlerException("The proxy URI (" + ApacheHttpClient4Config.PROPERTY_PROXY_URI +
                    ") property MUST be an instance of String or URI");
        }
    }
}
