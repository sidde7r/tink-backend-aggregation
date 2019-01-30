package se.tink.backend.aggregation.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import java.io.ByteArrayInputStream;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.MDC;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.credentials_requests.CredentialsRequest;
import se.tink.libraries.credentials_requests.CredentialsRequestType;
import se.tink.libraries.user.rpc.User;
import static se.tink.backend.aggregation.cluster.identification.ClusterId.CLUSTER_ENVIRONMENT_HEADER;
import static se.tink.backend.aggregation.cluster.identification.ClusterId.CLUSTER_NAME_HEADER;

public class AggregationLoggerRequestFilter implements ContainerRequestFilter {
    // Changes to these keys MUST be mirrored in:
    // - tink-backend/etc/development-aggregation-server.yml
    // - tink-infrastructure/states/tink/aggregation/aggregation-server.yml
    // - tink-infrastructure/states/infrastructure/elk/logstash/conf.d/04-tink-log-filter.conf
    private static final String CLUSTER_ID_MDC_KEY = "clusterId";
    private static final String CREDENTIALS_ID_MDC_KEY = "credentialsId";
    private static final String USER_ID_MDC_KEY = "userId";
    private static final String PROVIDER_NAME_MDC_KEY = "providerName";
    private static final String AGENT_NAME_MDC_KEY = "agentName";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static class CredentialsRequestImpl extends CredentialsRequest {
        public CredentialsRequestImpl() {
        }

        public CredentialsRequestImpl(User user, Provider provider, Credentials credentials) {
            super(user, provider, credentials);
        }

        @Override
        public boolean isManual() {
            return false;
        }

        @Override
        public CredentialsRequestType getType() {
            return CredentialsRequestType.CREATE;
        }
    }

    private void extractClusterId(ContainerRequest request) {
        ClusterId clusterId;

        if (Objects.isNull(request)) {
            clusterId = ClusterId.createEmpty();
        } else {
            String clusterName = request.getHeaderValue(CLUSTER_NAME_HEADER);
            String clusterEnvironment = request.getHeaderValue(CLUSTER_ENVIRONMENT_HEADER);
            clusterId = ClusterId.of(clusterName, clusterEnvironment);
        }

        if (clusterId.isValidId()) {
            MDC.put(CLUSTER_ID_MDC_KEY, clusterId.getId());
        }
    }

    private void extractCredentialsInformation(ContainerRequest request) {
        try {
            byte[] body = request.getEntity(byte[].class);
            if (Objects.isNull(body) || body.length == 0) {
                return;
            }

            // Put the body back into the input stream
            request.setEntityInputStream(new ByteArrayInputStream(body));

            // Try to parse it as a plain CredentialsRequest (impl above)
            CredentialsRequestImpl credentialsRequest = OBJECT_MAPPER.readValue(body, CredentialsRequestImpl.class);
            Credentials credentials = credentialsRequest.getCredentials();
            User user = credentialsRequest.getUser();
            Provider provider = credentialsRequest.getProvider();

            if (Objects.isNull(credentials) || Objects.isNull(user) || Objects.isNull(provider)) {
                // It was most likely not a `CredentialsRequest`.
                return;
            }

            MDC.put(CREDENTIALS_ID_MDC_KEY, Optional.ofNullable(credentials.getId()).orElse("-"));
            MDC.put(USER_ID_MDC_KEY, Optional.ofNullable(user.getId()).orElse("-"));
            MDC.put(PROVIDER_NAME_MDC_KEY, Optional.ofNullable(provider.getName()).orElse("-"));
            MDC.put(AGENT_NAME_MDC_KEY, Optional.ofNullable(provider.getClassName()).orElse("-"));
        } catch (Exception e) {
            // nop
        }
    }

    private void clearMdcKeys() {
        // The values must be removed so that previous thread information is not lingering.
        MDC.remove(CLUSTER_ID_MDC_KEY);
        MDC.remove(CREDENTIALS_ID_MDC_KEY);
        MDC.remove(USER_ID_MDC_KEY);
        MDC.remove(PROVIDER_NAME_MDC_KEY);
        MDC.remove(AGENT_NAME_MDC_KEY);
    }

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        clearMdcKeys();
        extractClusterId(request);
        extractCredentialsInformation(request);

        return request;
    }
}
