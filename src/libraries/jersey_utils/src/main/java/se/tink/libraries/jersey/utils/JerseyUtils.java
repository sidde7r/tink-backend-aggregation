package se.tink.libraries.jersey.utils;

import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.filter.Filterable;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import se.tink.libraries.tracing.jersey.filter.ClientTracingFilter;

@Slf4j
public class JerseyUtils {
    public static Client getClient(List<String> pinnedCertificates) {
        return new InterContainerJerseyClientFactory(getPinnedCertificates(pinnedCertificates))
                .build();
    }

    public static Client getClusterClient(
            byte[] clientCertificate,
            String clientCertificatePassword,
            boolean disableRequestCompression,
            ClientConfig config) {

        Instant start = Instant.now();
        InterClusterJerseyClientFactory interClusterJerseyClientFactory =
                new InterClusterJerseyClientFactory(config);

        if (disableRequestCompression) {
            interClusterJerseyClientFactory.disableRequestCompression();
        }

        if (Objects.nonNull(clientCertificate) && clientCertificate.length != 0) {
            interClusterJerseyClientFactory.withClientCertificate(
                    clientCertificate, clientCertificatePassword);
        }

        Client client = interClusterJerseyClientFactory.build();
        client.addFilter(new ClientTracingFilter());
        log.debug("Build client in {}ms", Duration.between(start, Instant.now()).toMillis());

        return client;
    }

    public static void registerAPIAccessToken(Filterable filterable, String accessToken) {
        if (accessToken != null) {
            ContainerApiTokenClientFilter.decorate(filterable, accessToken);
        }
    }

    private static List<String> getPinnedCertificates(List<String> pinnedCertificates) {
        if (pinnedCertificates == null) {
            return ImmutableList.of();
        } else {
            return pinnedCertificates;
        }
    }
}
