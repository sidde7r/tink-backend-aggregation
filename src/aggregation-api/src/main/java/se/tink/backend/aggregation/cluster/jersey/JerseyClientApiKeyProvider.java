package se.tink.backend.aggregation.cluster.jersey;

import com.google.inject.Inject;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import se.tink.backend.aggregation.cluster.annotations.ClientContext;
import se.tink.backend.aggregation.cluster.exceptions.ClientNotValid;
import se.tink.backend.aggregation.cluster.identification.ClientApiKey;

public class JerseyClientApiKeyProvider extends AbstractHttpContextInjectable<ClientApiKey>
        implements InjectableProvider<ClientContext, Type> {

    private static final String CLIENT_API_KEY_HEADER = ClientApiKey.CLIENT_API_KEY_HEADER;
    private ClientApiKeyProvider clientApiKey;

    @Inject
    public JerseyClientApiKeyProvider(ClientApiKeyProvider clientApiKey) {
        this.clientApiKey = clientApiKey;
    }

    @Override
    public Injectable<ClientApiKey> getInjectable(ComponentContext ic, ClientContext a, Type c) {
        return c.equals(ClientApiKey.class) ? this : null;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public ClientApiKey getValue(HttpContext c) {
        HttpRequestContext request = c.getRequest();

        try {
            return clientApiKey.getClientApiKey(request.getHeaderValue(CLIENT_API_KEY_HEADER));
        } catch (ClientNotValid clusterNotValid) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }
}
