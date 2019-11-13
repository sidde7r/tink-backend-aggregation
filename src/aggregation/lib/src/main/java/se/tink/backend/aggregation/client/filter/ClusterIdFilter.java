package se.tink.backend.aggregation.client.filter;

import com.google.common.base.Preconditions;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

public class ClusterIdFilter extends ClientFilter {
    private static final String CLUSTER_NAME_HEADER = "x-tink-cluster-name";
    private static final String CLUSTER_ENVIRONMENT_HEADER = "x-tink-cluster-environment";
    private final String clusterName;
    private final String clusterEnvironment;

    public ClusterIdFilter(String clusterName, String clusterEnvironment) {
        this.clusterName = clusterName;
        this.clusterEnvironment = clusterEnvironment;
    }

    public ClusterIdFilter(String clusterId) {
        String[] split = clusterId.split("-");
        Preconditions.checkState(
                split.length == 2,
                "Trying to create a ClusterIdFilter from an invalid clusterId : " + clusterId);
        this.clusterName = split[0];
        this.clusterEnvironment = split[1];
    }

    @Override
    public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
        cr.getHeaders().add(CLUSTER_NAME_HEADER, clusterName);
        cr.getHeaders().add(CLUSTER_ENVIRONMENT_HEADER, clusterEnvironment);
        return getNext().handle(cr);
    }
}
