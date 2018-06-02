package se.tink.backend.connector.auth;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import se.tink.libraries.http.annotations.auth.AllowAnonymous;
import se.tink.libraries.http.annotations.auth.AllowClient;
import se.tink.libraries.auth.AuthorizationResourceFilter;
import se.tink.backend.utils.LogUtils;

public class ConnectorAuthorizationResourceFilterFactory implements ResourceFilterFactory {

    private static final LogUtils log = new LogUtils(ConnectorAuthorizationResourceFilterFactory.class);

    @Context
    private HttpServletRequest request;

    private ConnectorAuthorizationFilterPredicates authenticationHeaderPredicates;

    @Inject
    public ConnectorAuthorizationResourceFilterFactory(
            ConnectorAuthorizationFilterPredicates authenticationHeaderPredicates) {
        this.authenticationHeaderPredicates = authenticationHeaderPredicates;
    }

    private static boolean isMethodWhitelisted(AbstractMethod abstractMethod) {
        String className = abstractMethod.getMethod().getDeclaringClass().getName();

        // The Swagger API is always allowed.
        if (className.equals("io.swagger.jaxrs.listing.ApiListingResource")) {
            return true;
        }

        return abstractMethod.isAnnotationPresent(AllowAnonymous.class);
    }

    protected static AllowClient getAllowedClient(AbstractMethod abstractMethod) {

        if (abstractMethod.getMethod().getDeclaringClass().isAnnotationPresent((AllowClient.class))) {
            return abstractMethod.getMethod().getDeclaringClass().getAnnotation(AllowClient.class);
        }

        return null;
    }

    @Override
    public List<ResourceFilter> create(AbstractMethod abstractMethod) {

        // Only allow whitelist on method level
        if (isMethodWhitelisted(abstractMethod)) {
            return ImmutableList.of();
        } else {

            AllowClient allowedClient = getAllowedClient(abstractMethod);

            Preconditions.checkNotNull(allowedClient, "No client annotation available on resource");

            Predicate<String> predicate = authenticationHeaderPredicates.getPredicateByClient(allowedClient.value());

            return ImmutableList.<ResourceFilter>of(new AuthorizationResourceFilter(log, predicate, request));
        }
    }
}
