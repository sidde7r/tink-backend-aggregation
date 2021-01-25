package se.tink.backend.aggregation.log;

import static se.tink.backend.aggregation.cluster.identification.ClusterId.CLUSTER_ENVIRONMENT_HEADER;
import static se.tink.backend.aggregation.cluster.identification.ClusterId.CLUSTER_NAME_HEADER;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import java.io.ByteArrayInputStream;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.user.rpc.User;

public class AggregationLoggerRequestFilter implements ContainerRequestFilter {
    private static final Logger logger =
            LoggerFactory.getLogger(AggregationLoggerRequestFilter.class);
    private static final String APP_ID_HEADER_KEY = "X-Tink-App-Id";

    private static final String CLUSTER_ID_MDC_KEY = "clusterId";
    private static final String CREDENTIALS_ID_MDC_KEY = "credentialsId";
    private static final String USER_ID_MDC_KEY = "userId";
    private static final String PROVIDER_NAME_MDC_KEY = "providerName";
    private static final String AGENT_NAME_MDC_KEY = "agentName";
    private static final String APP_ID_MDC_KEY = "appId";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static class CredentialsRequestImpl extends CredentialsRequest {
        public CredentialsRequestImpl() {}

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
            CredentialsRequestImpl credentialsRequest =
                    OBJECT_MAPPER.readValue(body, CredentialsRequestImpl.class);
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
            logger.warn("Failed to extract credentials information for logging.", e);
        }
    }

    private void clearMdcKeys() {
        // The values must be removed so that previous thread information is not lingering.
        MDC.remove(CLUSTER_ID_MDC_KEY);
        MDC.remove(CREDENTIALS_ID_MDC_KEY);
        MDC.remove(USER_ID_MDC_KEY);
        MDC.remove(PROVIDER_NAME_MDC_KEY);
        MDC.remove(AGENT_NAME_MDC_KEY);
        MDC.remove(APP_ID_MDC_KEY);
    }

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        clearMdcKeys();
        extractClusterId(request);
        extractCredentialsInformation(request);
        extractAppId(request);

        return request;
    }

    private void extractAppId(ContainerRequest request) {
        String appIdFromHeader = request.getHeaderValue(APP_ID_HEADER_KEY);

        if (appIdFromHeader == null || appIdFromHeader.isEmpty()) {
            MDC.put(APP_ID_MDC_KEY, "unknown");
        } else {
            MDC.put(APP_ID_MDC_KEY, appIdFromHeader);
        }
    }
}
