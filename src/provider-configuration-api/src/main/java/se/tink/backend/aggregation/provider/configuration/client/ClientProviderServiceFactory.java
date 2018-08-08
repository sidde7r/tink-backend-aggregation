package se.tink.backend.aggregation.provider.configuration.client;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;
import java.util.Objects;
import se.tink.backend.aggregation.provider.configuration.api.MonitoringService;
import se.tink.backend.aggregation.provider.configuration.api.ProviderService;
import se.tink.libraries.http.client.BasicWebServiceClassBuilder;
import se.tink.libraries.http.client.ServiceClassBuilder;
import se.tink.libraries.jersey.utils.JerseyUtils;

public class ClientProviderServiceFactory implements ProviderServiceFactory{
    private ServiceClassBuilder builder;

    public ClientProviderServiceFactory(byte[] pinnedCertificates, String host, int port, ClientFilter clientFilter){
        this(new BasicWebServiceClassBuilder(createResource(pinnedCertificates, host,clientFilter)));
    }

    private static WebResource createResource(byte[] pinnedCertificates,
            String host, ClientFilter clientFilter){

        Preconditions.checkState(!Strings.isNullOrEmpty(host),
                "Host cannot be empty");
        Preconditions.checkState(!Objects.isNull(clientFilter),
                "The provider service requires ");

        Client client = JerseyUtils.getClusterClient(pinnedCertificates, "",false);
        client.addFilter(clientFilter);
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
