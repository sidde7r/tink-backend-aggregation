package se.tink.backend.aggregation.provider.configuration.client;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.provider.configuration.api.MonitoringService;
import se.tink.backend.aggregation.provider.configuration.api.ProviderService;
import se.tink.libraries.http.client.BasicWebServiceClassBuilder;
import se.tink.libraries.http.client.ServiceClassBuilder;
import se.tink.libraries.jersey.utils.JerseyUtils;

public class ClientProviderServiceFactory implements ProviderServiceFactory{
    private ServiceClassBuilder builder;

    public ClientProviderServiceFactory(List<String> pinnedCertificates, String host, ClientFilter clientFilter, String accessToken){
        this(new BasicWebServiceClassBuilder(createResource(pinnedCertificates, host, clientFilter, accessToken)));
    }

    private static WebResource createResource(List<String> pinnedCertificates,
            String host, ClientFilter clientFilter, String accessToken){

        Preconditions.checkState(!Strings.isNullOrEmpty(host),
                "Host cannot be empty");
        Preconditions.checkState(!Objects.isNull(clientFilter),
                "The provider service requires");
        Client client = JerseyUtils.getClient(pinnedCertificates);
        client.addFilter(clientFilter);
        JerseyUtils.registerAPIAccessToken(client, accessToken);
        return client.resource(host);
    }

    private ClientProviderServiceFactory(ServiceClassBuilder builder) {
        this.builder = builder;
    }

    @Override
    public ProviderService getProviderSerive() {
        return builder.build(ProviderService.class);
    }

    @Override
    public MonitoringService getMonitoringService() {
        return builder.build(MonitoringService.class);
    }
}
