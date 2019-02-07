package se.tink.libraries.auth;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.http.annotations.auth.AllowAnonymous;

public class ContainerAuthorizationResourceFilterFactory implements ResourceFilterFactory {

    private static final Logger log = LoggerFactory.getLogger(ContainerAuthorizationResourceFilterFactory.class);
    
    @Context
    private HttpServletRequest request;

    private Predicate<String> authenticationHeaderPredicate;

    public ContainerAuthorizationResourceFilterFactory(Predicate<String> authenticationHeaderPredicate) {
        this.authenticationHeaderPredicate = Preconditions.checkNotNull(authenticationHeaderPredicate);
    }

    private static boolean isWhitelisted(AbstractMethod abstractMethod) {
        return abstractMethod.isAnnotationPresent(AllowAnonymous.class)
                || abstractMethod.getResource().isAnnotationPresent(AllowAnonymous.class);
    }

    @Override
    public List<ResourceFilter> create(AbstractMethod abstractMethod) {
        if (isWhitelisted(abstractMethod)) {
            return ImmutableList.of();
        } else {
            return ImmutableList.<ResourceFilter>of(
                    new AuthorizationResourceFilter(log, authenticationHeaderPredicate, request));
        }
    }

}


