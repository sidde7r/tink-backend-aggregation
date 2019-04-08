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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;

/**
 * A {@link Client} that utilizes the Apache HTTP Client to send and receive HTTP request and
 * responses.
 *
 * <p>The following properties are only supported at construction of this class: {@link
 * ApacheHttpClient4Config#PROPERTY_CONNECTION_MANAGER}<br>
 * {@link ApacheHttpClient4Config#PROPERTY_HTTP_PARAMS}}<br>
 * {@link ApacheHttpClient4Config#PROPERTY_CREDENTIALS_PROVIDER}}<br>
 * {@link ApacheHttpClient4Config#PROPERTY_DISABLE_COOKIES}}<br>
 * {@link ApacheHttpClient4Config#PROPERTY_PROXY_URI}}<br>
 * {@link ApacheHttpClient4Config#PROPERTY_PROXY_USERNAME}}<br>
 * {@link ApacheHttpClient4Config#PROPERTY_PROXY_PASSWORD}}<br>
 * {@link ApacheHttpClient4Config#PROPERTY_PREEMPTIVE_BASIC_AUTHENTICATION}}<br>
 *
 * <p>By default a request entity is buffered and repeatable such that authorization may be
 * performed automatically in response to a 401 response.
 *
 * <p>If the property {@link ClientConfig#PROPERTY_CHUNKED_ENCODING_SIZE} size is set to a value
 * greater than 0 then chunked encoding will be enabled and the request entity (if present) will not
 * be buffered and is not repeatable. For authorization to work in such scenarios the property
 * {@link ApacheHttpClient4Config#PROPERTY_PREEMPTIVE_BASIC_AUTHENTICATION} must be set to true.
 *
 * <p>If a {@link com.sun.jersey.api.client.ClientResponse} is obtained and an entity is not read
 * from the response then {@link com.sun.jersey.api.client.ClientResponse#close() } MUST be called
 * after processing the response to release connection-based resources.
 *
 * @see ApacheHttpClient4Config#PROPERTY_CONNECTION_MANAGER
 * @author jorgeluisw@mac.com
 * @author Paul.Sandoz@Sun.Com
 * @author pavel.bucek@oracle.com
 */
public class TinkApacheHttpClient4 extends Client {

    private final TinkApacheHttpClient4Handler client4Handler;

    /**
     * Create a new client instance with a client configuration.
     *
     * @param root the root client handler for dispatching a request and returning a response.
     * @param config the client configuration.
     */
    public TinkApacheHttpClient4(
            final TinkApacheHttpClient4Handler root, final ClientConfig config) {
        this(root, config, null);
    }

    /**
     * Create a new instance with a client configuration and a component provider.
     *
     * @param root the root client handler for dispatching a request and returning a response.
     * @param config the client configuration.
     * @param provider the IoC component provider factory.
     */
    public TinkApacheHttpClient4(
            final TinkApacheHttpClient4Handler root,
            final ClientConfig config,
            final IoCComponentProviderFactory provider) {
        super(root, config, provider);

        this.client4Handler = root;
    }
}
