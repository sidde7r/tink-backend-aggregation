package se.tink.libraries.jersey.utils;

import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.Filterable;
import java.util.List;
import java.util.Objects;

public class JerseyUtils {
    public static Client getClient(List<String> pinnedCertificates) {
        return new InterContainerJerseyClientFactory(getPinnedCertificates(pinnedCertificates))
                .build();
    }

    public static Client getClusterClient(
            byte[] clientCertificate,
            String clientCertificatePassword,
            boolean disableRequestCompression) {
        InterClusterJerseyClientFactory interClusterJerseyClientFactory =
                new InterClusterJerseyClientFactory();

        if (disableRequestCompression) {
            interClusterJerseyClientFactory.disableRequestCompression();
        }

        if (Objects.nonNull(clientCertificate) && clientCertificate.length != 0) {
            interClusterJerseyClientFactory.withClientCertificate(
                    clientCertificate, clientCertificatePassword);
        }

        return interClusterJerseyClientFactory.build();
    }

    public static WebResource getResource(List<String> pinnedCertificates, String url) {
        return getClient(pinnedCertificates).resource(url);
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
