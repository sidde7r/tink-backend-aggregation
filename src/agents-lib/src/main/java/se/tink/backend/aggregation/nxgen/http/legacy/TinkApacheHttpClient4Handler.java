package se.tink.backend.aggregation.nxgen.http.legacy;
/*
    NOTE:
        `TinkApacheHttpClient4Handler` and `TinkApacheHttpClient4` are only used because the version of
        `ApacheHttpClient4Handler` that we use has a bug when it comes to `Transfer-Encoding: chunked`
        (We cannot disable it).
        Todo: Remove these two temporary classes when we upgrade to a newer ApacheHttpClient4 library.
 */

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

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.TerminatingClientHandler;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.core.util.ReaderWriter;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.org.apache.http.Header;
import se.tink.org.apache.http.HttpEntity;
import se.tink.org.apache.http.HttpHost;
import se.tink.org.apache.http.HttpResponse;
import se.tink.org.apache.http.client.HttpClient;
import se.tink.org.apache.http.client.methods.HttpDelete;
import se.tink.org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import se.tink.org.apache.http.client.methods.HttpGet;
import se.tink.org.apache.http.client.methods.HttpHead;
import se.tink.org.apache.http.client.methods.HttpOptions;
import se.tink.org.apache.http.client.methods.HttpPost;
import se.tink.org.apache.http.client.methods.HttpPut;
import se.tink.org.apache.http.client.methods.HttpUriRequest;
import se.tink.org.apache.http.client.protocol.HttpClientContext;
import se.tink.org.apache.http.entity.AbstractHttpEntity;
import se.tink.org.apache.http.entity.BufferedHttpEntity;
import se.tink.org.apache.http.impl.client.DefaultRedirectStrategy;
import se.tink.org.apache.http.protocol.HttpContext;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;

/**
 * A root handler with Apache HTTP Client acting as a backend.
 * <p>
 * Client operations are thread safe, the HTTP connection may be shared between
 * different threads.
 * <p>
 * If a response entity is obtained that is an instance of {@link Closeable}
 * then the instance MUST be closed after processing the entity to release
 * connection-based resources.
 * <p>
 * If a {@link ClientResponse} is obtained and an entity is not read from the
 * response then {@link ClientResponse#close() } MUST be called after processing
 * the response to release connection-based resources.
 * <p>
 * The following methods are currently supported: HEAD, GET, POST, PUT, DELETE
 * and OPTIONS.
 * <p>
 * Chunked transfer encoding can be enabled or disabled but configuration of the
 * chunked encoding size is not possible. If the
 * {@link ClientConfig#PROPERTY_CHUNKED_ENCODING_SIZE} property is set to a
 * non-null value then chunked transfer encoding is enabled.
 * 
 * @author jorgeluisw@mac.com
 * @author Paul.Sandoz@Sun.Com
 * @author pavel.bucek@oracle.com
 */
public final class TinkApacheHttpClient4Handler extends TerminatingClientHandler {

    private final HttpClient client;

    /**
     * Create a new root handler with an {@link HttpClient}.
     * 
     * @param client
     *            the {@link HttpClient}.
     */
    public TinkApacheHttpClient4Handler(final HttpClient client) {
        this.client = client;
    }

    public ClientResponse handle(final ClientRequest cr) throws ClientHandlerException {

        final HttpUriRequest request = getUriHttpRequest(cr);

        writeOutBoundHeaders(cr.getHeaders(), request);

        try {
            // The `context` is passed along to all of Apache's http handlers; RedirectStrategy for example.
            HttpContext context = new HttpClientContext();
            HttpResponse response = client.execute(getHost(request), request, context);

            ClientResponse r = new ClientResponse(
                                            response.getStatusLine().getStatusCode(),
                                            getInBoundHeaders(response),
                                            new HttpClientResponseInputStream(response),
                                            getMessageBodyWorkers());

            // Store the redirects on the Jersey response
            Object redirects = context.getAttribute(DefaultRedirectStrategy.REDIRECT_LOCATIONS);
            r.getProperties().put(DefaultRedirectStrategy.REDIRECT_LOCATIONS, redirects);

            if (!r.hasEntity()) {
                r.bufferEntity();
                r.close();
            }
            return r;
        } catch (Exception e) {
            throw new HttpClientException(e, null);
        }
    }

    private HttpHost getHost(final HttpUriRequest request) {
        return new HttpHost(request.getURI().getHost(), request.getURI().getPort(), request.getURI().getScheme());
    }

    private HttpUriRequest getUriHttpRequest(final ClientRequest cr) {
        final String strMethod = cr.getMethod();
        final URI uri = cr.getURI();

        final HttpEntity entity = getHttpEntity(cr);
        final HttpUriRequest request;

        if (strMethod.equals("GET")) {
            request = new HttpGet(uri);
        } else if (strMethod.equals("POST")) {
            request = new HttpPost(uri);
        } else if (strMethod.equals("PUT")) {
            request = new HttpPut(uri);
        } else if (strMethod.equals("DELETE")) {
            request = new HttpDelete(uri);
        } else if (strMethod.equals("HEAD")) {
            request = new HttpHead(uri);
        } else if (strMethod.equals("OPTIONS")) {
            request = new HttpOptions(uri);
        } else {
            request = new HttpEntityEnclosingRequestBase() {
                @Override
                public String getMethod() {
                    return strMethod;
                }

                @Override
                public URI getURI() {
                    return uri;
                }
            };
        }

        if (entity != null && request instanceof HttpEntityEnclosingRequestBase) {
            ((HttpEntityEnclosingRequestBase) request).setEntity(entity);
        } else if (entity != null) {
            throw new ClientHandlerException("Adding entity to http method " + cr.getMethod() + " is not supported.");
        }

        return request;
    }

    private HttpEntity getHttpEntity(final ClientRequest cr) {
        final Object entity = cr.getEntity();

        if (entity == null) {
            return null;
        }

        final RequestEntityWriter requestEntityWriter = getRequestEntityWriter(cr);

        try {
            HttpEntity httpEntity = new AbstractHttpEntity() {
                @Override
                public boolean isRepeatable() {
                    return false;
                }

                @Override
                public long getContentLength() {
                    return requestEntityWriter.getSize();
                }

                @Override
                public InputStream getContent() throws IOException, IllegalStateException {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream(512);
                    writeTo(buffer);
                    return new ByteArrayInputStream(buffer.toByteArray());
                }

                @Override
                public void writeTo(OutputStream outputStream) throws IOException {
                    requestEntityWriter.writeRequestEntity(outputStream);
                }

                @Override
                public boolean isStreaming() {
                    return false;
                }
            };

            if (cr.getProperties().get(ClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE) != null) {
                // TODO return InputStreamEntity
                return httpEntity;
            } else {
                return new BufferedHttpEntity(httpEntity);
            }
        } catch (Exception ex) {
            // TODO warning/error?
        }

        return null;
    }

    private void writeOutBoundHeaders(final MultivaluedMap<String, Object> headers, final HttpUriRequest request) {
        for (Map.Entry<String, List<Object>> e : headers.entrySet()) {
            List<Object> vs = e.getValue();
            if (vs.size() == 1) {
                request.addHeader(e.getKey(), ClientRequest.getHeaderValue(vs.get(0)));
            } else {
                StringBuilder b = new StringBuilder();
                for (Object v : e.getValue()) {
                    if (b.length() > 0) {
                        b.append(',');
                    }
                    b.append(ClientRequest.getHeaderValue(v));
                }
                request.addHeader(e.getKey(), b.toString());
            }
        }
    }

    private InBoundHeaders getInBoundHeaders(final HttpResponse response) {
        final InBoundHeaders headers = new InBoundHeaders();
        final Header[] respHeaders = response.getAllHeaders();
        for (Header header : respHeaders) {
            List<String> list = headers.get(header.getName());
            if (list == null) {
                list = new ArrayList<String>();
            }
            list.add(header.getValue());
            headers.put(header.getName(), list);
        }
        return headers;
    }

    private static final class HttpClientResponseInputStream extends FilterInputStream {

        HttpClientResponseInputStream(final HttpResponse response) throws IOException {
            super(getInputStream(response));
        }

        @Override
        public void close() throws IOException {
            super.close();
        }
    }

    private static InputStream getInputStream(final HttpResponse response) throws IOException {

        if (response.getEntity() == null) {
            return new ByteArrayInputStream(new byte[0]);
        } else {
            final InputStream i = response.getEntity().getContent();
            if (i.markSupported()) {
                return i;
            }
            return new BufferedInputStream(i, ReaderWriter.BUFFER_SIZE);
        }
    }
}
