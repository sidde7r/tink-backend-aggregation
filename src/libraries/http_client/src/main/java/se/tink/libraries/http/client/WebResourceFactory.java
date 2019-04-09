package se.tink.libraries.http.client;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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

import com.google.common.base.Splitter;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.libraries.request_tracing.RequestTracer;

/**
 * Factory for client-side representation of a resource. See the <a
 * href="package-summary.html">package overview</a> for an example on how to use this class.
 *
 * @author Martin Matula (martin.matula at oracle.com)
 *     <p>Adapted for JAX-RS 1 by @author mkalam-alami
 */
public final class WebResourceFactory implements InvocationHandler {

    private static final String FALLBACK_CONTENT_TYPE = MediaType.APPLICATION_JSON;
    private final WebResource target;
    private final List<Cookie> cookies;
    private final Form form;

    private static final Form EMPTY_FORM = new Form();

    /**
     * Creates a new client-side representation of a resource described by the interface passed in
     * the first argument.
     *
     * <p>Calling this method has the same effect as calling {@code
     * WebResourceFactory.newResource(resourceInterface, rootTarget, false)} .
     *
     * @param <T> Type of the resource to be created.
     * @param resourceInterface Interface describing the resource to be created.
     * @param target WebResource pointing to the resource or the parent of the resource.
     * @return Instance of a class implementing the resource interface that can be used for making
     *     requests to the server.
     */
    public static <T> T newResource(Class<T> resourceInterface, WebResource target) {
        return newResource(resourceInterface, target, false, Collections.emptyList(), EMPTY_FORM);
    }

    /**
     * Creates a new client-side representation of a resource described by the interface passed in
     * the first argument.
     *
     * @param <C> Type of the resource to be created.
     * @param resourceInterface Interface describing the resource to be created.
     * @param target WebResource pointing to the resource or the parent of the resource.
     * @param ignoreResourcePath If set to true, ignores path annotation on the resource interface
     *     (this is used when creating sub-resources)
     * @param cookies Cookie params collected from parent resources (used when creating a
     *     sub-resource)
     * @param form Form params collected from parent resources (used when creating a sub-resource)
     * @return Instance of a class implementing the resource interface that can be used for making
     *     requests to the server.
     */
    @SuppressWarnings("unchecked")
    private static <C> C newResource(
            Class<C> resourceInterface,
            WebResource target,
            boolean ignoreResourcePath,
            List<Cookie> cookies,
            Form form) {
        return (C)
                Proxy.newProxyInstance(
                        resourceInterface.getClassLoader(),
                        new Class[] {resourceInterface},
                        new WebResourceFactory(
                                ignoreResourcePath
                                        ? target
                                        : addPathFromAnnotation(resourceInterface, target),
                                cookies,
                                form));
    }

    private MultivaluedMap<String, String> requestIdHeaders() {
        return Optional.ofNullable(RequestTracer.getRequestId())
                .map(
                        id -> {
                            MultivaluedMap<String, String> newHeaders = new MultivaluedMapImpl();
                            newHeaders.putSingle(RequestTracingFilter.REQUEST_ID_HEADER, id);
                            return newHeaders;
                        })
                .orElse(new MultivaluedMapImpl());
    }

    private WebResourceFactory(WebResource target, List<Cookie> cookies, Form form) {
        this.target = target;
        this.cookies = cookies;
        this.form = form;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // get the interface describing the resource
        Class<?> proxyIfc = proxy.getClass().getInterfaces()[0];

        // response type
        Class<?> responseType = method.getReturnType();

        // determine method name
        String httpMethod = getHttpMethodName(method);
        if (httpMethod == null) {
            for (Annotation ann : method.getAnnotations()) {
                httpMethod = getHttpMethodName(ann.annotationType());
                if (httpMethod != null) {
                    break;
                }
            }
        }

        // create a new UriBuilder appending the @Path attached to the method
        WebResource newTarget;
        Path p = method.getAnnotation(Path.class);
        if (p != null) {
            newTarget = addPathFromAnnotation(method, target);
        } else {
            newTarget = target;
        }

        if (httpMethod == null) {
            if (newTarget == target) {
                // no path annotation on the method -> fail
                throw new UnsupportedOperationException("Not a resource method.");
            } else if (!responseType.isInterface()) {
                // the method is a subresource locator, but returns class,
                // not interface - can't help here
                throw new UnsupportedOperationException("Return type not an interface");
            }
        }

        // process method params (build maps of
        // (Path|Form|Cookie|Matrix|Header..)Params
        // and extract entity type
        MultivaluedMap<String, String> headers = requestIdHeaders();
        LinkedList<Cookie> cookies = new LinkedList<Cookie>(this.cookies);
        Form form = new Form();
        form.putAll(this.form);
        Annotation[][] paramAnns = method.getParameterAnnotations();
        Object entity = null;
        Type entityType = null;
        for (int i = 0; i < paramAnns.length; i++) {
            Map<Class<?>, Annotation> anns = new HashMap<Class<?>, Annotation>();
            for (Annotation ann : paramAnns[i]) {
                Class<? extends Annotation> annotationType = ann.annotationType();
                if (methodParameterAnnotationIsIgnored(annotationType)) {
                    continue;
                }
                anns.put(annotationType, ann);
            }
            Annotation ann;
            Object value = args[i];
            if (anns.isEmpty()) {
                entityType = method.getGenericParameterTypes()[i];
                entity = value;
            } else {
                if (value == null && (ann = anns.get(DefaultValue.class)) != null) {
                    value = ((DefaultValue) ann).value();
                }
                if (value != null) {
                    if ((ann = anns.get(PathParam.class)) != null) {
                        // TODO: HACK!
                        String uri = URLDecoder.decode(newTarget.getURI().getPath());

                        String newUri =
                                uri.replace(
                                        "{" + ((PathParam) ann).value() + "}", value.toString());
                        newTarget = newTarget.uri(new URI(newUri));
                        // newTarget = newTarget.pathParam(((PathParam)
                        // ann).value(), (String) value);
                    } else if ((ann = anns.get((QueryParam.class))) != null) {
                        newTarget =
                                newTarget.queryParam(((QueryParam) ann).value(), (String) value);
                    } else if ((ann = anns.get((HeaderParam.class))) != null) {
                        headers.add(((HeaderParam) ann).value(), (String) value);
                    } else if ((ann = anns.get((CookieParam.class))) != null) {
                        String name = ((CookieParam) ann).value();
                        Cookie c;
                        if (!(value instanceof Cookie)) {
                            c = new Cookie(name, value.toString());
                        } else {
                            c = (Cookie) value;
                            if (!name.equals(((Cookie) value).getName())) {
                                // is this the right thing to do? or should I
                                // fail? or ignore the difference?
                                c =
                                        new Cookie(
                                                name,
                                                c.getValue(),
                                                c.getPath(),
                                                c.getDomain(),
                                                c.getVersion());
                            }
                        }
                        cookies.add(c);
                    } else if ((ann = anns.get((MatrixParam.class))) != null) {
                        throw new UnsupportedOperationException();
                        // newTarget = newTarget.matrixParam(((MatrixParam)
                        // ann).value(), value);
                    } else if ((ann = anns.get((FormParam.class))) != null) {
                        form.add(((FormParam) ann).value(), value.toString());
                    }
                }
            }
        }

        if (httpMethod == null) {
            // the method is a subresource locator
            return WebResourceFactory.newResource(responseType, newTarget, true, cookies, form);
        }

        // accepted media types
        Produces produces = method.getAnnotation(Produces.class);
        if (produces == null) {
            produces = proxyIfc.getAnnotation(Produces.class);
        }
        String[] accepts = produces == null ? null : produces.value();

        // determine content type
        String contentType = null;
        if (entity != null) {
            Consumes consumes = method.getAnnotation(Consumes.class);
            if (consumes == null) {
                consumes = proxyIfc.getAnnotation(Consumes.class);
            }
            if (consumes != null && consumes.value().length > 0) {
                // TODO: should consider q/qs instead of picking the first one
                contentType = consumes.value()[0];
            }
        }

        Builder b;
        if (accepts != null) {
            b = newTarget.accept(accepts);
        } else {
            b = newTarget.getRequestBuilder();
        }

        // apply header params and cookies
        for (Cookie c : cookies) {
            b = b.cookie(c);
        }
        // TODO: change this to b.headers(headers) once we switch to the latest
        // JAX-RS API
        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            for (Object value : header.getValue()) {
                b = b.header(header.getKey(), value);
            }
        }

        Object result;

        if (entity == null && !form.isEmpty()) {
            entity = form;
            contentType = MediaType.APPLICATION_FORM_URLENCODED;
        } else {
            if (contentType == null) {
                contentType = FALLBACK_CONTENT_TYPE;
            }
            if (!form.isEmpty()) {
                if (entity instanceof Form) {
                    ((Form) entity).putAll(form);
                } else {
                    // TODO: should at least log some warning here
                }
            }
        }

        GenericType<?> responseGenericType = new GenericType<Object>(method.getGenericReturnType());

        if (entity != null) {
            if (entityType instanceof ParameterizedType) {
                entity = new GenericEntity<Object>(entity, entityType);
            }
            b.entity(entity, contentType);
        }

        Class<?> responseClass = responseGenericType.getRawClass();

        if (responseClass.equals(Void.TYPE)) {
            // Making the HTTP call, but don't return the result.
            b.method(httpMethod);

            result = null;
        } else {
            result = b.method(httpMethod, responseGenericType);
        }

        return result;
    }

    private static final Splitter PERIOD_SPLITTER = Splitter.on(".");

    private boolean methodParameterAnnotationIsIgnored(Class<? extends Annotation> ann) {
        Iterator<String> pieces = PERIOD_SPLITTER.split(ann.getName()).iterator();
        if (pieces.hasNext()) {
            String packageRoot = pieces.next();
            return !Objects.equals(packageRoot, "javax");
        }
        return false;
    }

    private static WebResource addPathFromAnnotation(AnnotatedElement ae, WebResource target) {
        Path p = ae.getAnnotation(Path.class);
        if (p != null) {
            target = target.path(p.value());
        }
        return target;
    }

    private static String getHttpMethodName(AnnotatedElement ae) {
        HttpMethod a = ae.getAnnotation(HttpMethod.class);
        return a == null ? null : a.value();
    }
}
