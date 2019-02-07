package se.tink.libraries.auth;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;

public class AuthorizationResourceFilter implements ContainerRequestFilter, ResourceFilter {

    private static final Splitter AUTH_SPLITTER = Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings().trimResults()
            .limit(2);

    private final Logger log;
    private final HttpServletRequest request;
    private final Predicate<String> authenticationHeaderPredicate;

    public AuthorizationResourceFilter(Logger log, Predicate<String> authenticationHeaderPredicate,
            HttpServletRequest request) {
        this.log = Preconditions.checkNotNull(log);
        this.authenticationHeaderPredicate = Preconditions.checkNotNull(authenticationHeaderPredicate);
        this.request = request;
    }

    @Override
    public ContainerRequest filter(ContainerRequest containerRequest) {

        final String authorizationHeaderValue = containerRequest.getHeaderValue(HttpHeaders.AUTHORIZATION);

        if (authorizationHeaderValue == null) {
            signalUnauthorized();
        }

        final ImmutableList<String> authorizationHeaderPieces = ImmutableList
                .copyOf(AUTH_SPLITTER.split(authorizationHeaderValue));

        if (authorizationHeaderPieces.size() != 2) {
            log.warn("Missing authorization method.");
            signalUnauthorized();
        }

        if (!authenticationHeaderPredicate.apply(authorizationHeaderValue)) {
            log.warn("Unauthorized user.");
            signalUnauthorized();
        }

        return containerRequest;

    }

    private void signalUnauthorized() {
        final Response response = Response.status(Response.Status.UNAUTHORIZED).entity("Not authorized.").build();
        log.error(
                String.format("[ip=%s url=%s] Unauthorized access.", request.getRemoteAddr(), request.getRequestURI()));
        throw new WebApplicationException(response);
    }

    @Override
    public ContainerRequestFilter getRequestFilter() {
        return this;
    }

    @Override
    public ContainerResponseFilter getResponseFilter() {
        return null; // Noop.
    }

}
