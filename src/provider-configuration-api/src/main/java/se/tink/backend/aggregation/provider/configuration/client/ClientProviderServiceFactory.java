package se.tink.backend.aggregation.provider.configuration.client;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;

import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.provider.configuration.api.MonitoringService;
import se.tink.backend.aggregation.provider.configuration.api.ProviderService;
import se.tink.libraries.http.client.BasicWebServiceClassBuilder;
import se.tink.libraries.http.client.ServiceClassBuilder;
import se.tink.libraries.jersey.utils.JerseyUtils;

public class ClientProviderServiceFactory implements ProviderServiceFactory{

    private List<String> pinnedCertificate;
    private String host;
    private String accessToken;

    public ClientProviderServiceFactory(List<String> pinnedCertificates, String host, String accessToken){
        this.pinnedCertificate = pinnedCertificates;
        this.host = host;
        this.accessToken = accessToken;
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

    private ServiceClassBuilder builder(String clusterName, String clusterEnvironment) {
        ClusterIdFilter clientFilter = new ClusterIdFilter(clusterName, clusterEnvironment);
        return new BasicWebServiceClassBuilder(createResource(pinnedCertificate, host,
                clientFilter, accessToken));
    }

    @Override
    public ProviderService getProviderSerive(String clusterName, String clusterEnvironment){
        return builder(clusterName, clusterEnvironment).build(ProviderService.class);
    }

    @Override
    public MonitoringService getMonitoringService(String clusterName, String clusterEnvironment){
        return builder(clusterName, clusterEnvironment).build(MonitoringService.class);
    }

    public class ClusterIdFilter extends ClientFilter {
        private final String clusterName;
        private final String clusterEnvironment;

        public ClusterIdFilter(String clusterName, String clusterEnvironment) {
            this.clusterName = clusterName;
            this.clusterEnvironment = clusterEnvironment;
        }

        @Override
        public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
            cr.getHeaders().add(ClusterId.CLUSTER_NAME_HEADER, clusterName);
            cr.getHeaders().add(ClusterId.CLUSTER_ENVIRONMENT_HEADER, clusterEnvironment);
            return getNext().handle(cr);
        }
    }
}
