package se.tink.backend.aggregation.cluster.jersey;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import java.lang.reflect.Type;
import org.assertj.core.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.cluster.annotations.ClientContext;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import static se.tink.backend.aggregation.cluster.jersey.JerseyClusterInfoProvider.CLUSTER_ENVIRONMENT_HEADER;
import static se.tink.backend.aggregation.cluster.jersey.JerseyClusterInfoProvider.CLUSTER_NAME_HEADER;

public class JerseyClientProvider extends AbstractHttpContextInjectable<ClientInfo>
        implements InjectableProvider<ClientContext, Type> {
    private static final String CLIENT_API_KEY_HEADER = "X-Tink-Client-Api-Key";

    private final Logger logger = LoggerFactory.getLogger(JerseyClientProvider.class);

    @Override
    public Injectable<ClientInfo> getInjectable(ComponentContext ic, ClientContext a, Type c) {
        return c.equals(ClientInfo.class) ? this : null;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public ClientInfo getValue(HttpContext c) {
        HttpRequestContext request = c.getRequest();
        String apiKey = request.getHeaderValue(CLIENT_API_KEY_HEADER);
        if (Strings.isNullOrEmpty(apiKey)) {
            String name = request.getHeaderValue(CLUSTER_NAME_HEADER);
            String environment = request.getHeaderValue(CLUSTER_ENVIRONMENT_HEADER);
            logger.error("Received a missing api key for {} {} .", name, environment);
        }

        return null ;
    }
}
