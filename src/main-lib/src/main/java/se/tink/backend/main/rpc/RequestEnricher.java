package se.tink.backend.main.rpc;

import java.util.Optional;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import java.lang.reflect.Type;
import se.tink.backend.auth.EnrichRequest;
import se.tink.backend.auth.OAuth2ClientRequest;
import se.tink.api.headers.TinkHttpHeaders;

public class RequestEnricher implements InjectableProvider<EnrichRequest, Type> {

    private final OAuth2ClientRequestEnricher enricher;

    public RequestEnricher(OAuth2ClientRequestEnricher enricher) {
        this.enricher = enricher;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable<?> getInjectable(ComponentContext componentContext, EnrichRequest enrichRequest, Type type) {

        if (type.equals(OAuth2ClientRequest.class)) {
            return new LinkRequestEnricherInjectable();
        }

        throw new IllegalStateException("Not implemented!");
    }

    private class LinkRequestEnricherInjectable extends AbstractHttpContextInjectable<OAuth2ClientRequest> {

        public LinkRequestEnricherInjectable() {
        }

        @Override
        public OAuth2ClientRequest getValue(HttpContext context) {

            HttpRequestContext request = context.getRequest();

            String clientKey = request.getHeaderValue(TinkHttpHeaders.CLIENT_KEY_HEADER_NAME);
            String oAuthClientId = request.getHeaderValue(TinkHttpHeaders.OAUTH_CLIENT_ID_HEADER_NAME);

            return enricher.createEnriched(
                    Optional.ofNullable(clientKey),
                    Optional.ofNullable(oAuthClientId));
        }
    }
}
