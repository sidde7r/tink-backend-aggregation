package se.tink.backend.aggregation.provider.configuration.client;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;
import java.util.Objects;
import se.tink.backend.aggregation.provider.configuration.api.MonitoringService;
import se.tink.backend.aggregation.provider.configuration.api.ProviderService;
import se.tink.libraries.http.client.BasicWebServiceClassBuilder;
import se.tink.libraries.http.client.ServiceClassBuilder;
import se.tink.libraries.jersey.utils.JerseyUtils;

public class InterClusterClientProviderServiceFactory implements InterClusterProviderServiceFactory {
    private ServiceClassBuilder builder;

    public InterClusterClientProviderServiceFactory(byte[] pinnedCertificates, String host, ClientFilter
            clientFilter, String accessToken){
        this(new BasicWebServiceClassBuilder(createResource(pinnedCertificates, host, clientFilter, accessToken)));
    }

    private InterClusterClientProviderServiceFactory(ServiceClassBuilder builder){ this.builder = builder; }

    private static WebResource createResource(byte[] pinnedCertificates, String host, ClientFilter clientFilter,
            String accessToken){
        Preconditions.checkState(!Strings.isNullOrEmpty(host), "Host cannot be empty");
        Preconditions.checkState(!Objects.isNull(clientFilter), "Cluster info missing");

        Client client = JerseyUtils.getClusterClient(pinnedCertificates, "", false);
        client.addFilter(clientFilter);
        JerseyUtils.registerAPIAccessToken(client, accessToken);
        return client.resource(host);
    }

    @Override
    public ProviderService getProviderService() {
        return builder.build(ProviderService.class);
    }

    @Override
    public MonitoringService getMonitoringService() {
        return builder.build(MonitoringService.class);
    }
}
