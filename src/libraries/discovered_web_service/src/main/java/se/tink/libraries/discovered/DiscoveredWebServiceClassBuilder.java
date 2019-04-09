package se.tink.libraries.discovered;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceInstance;
import se.tink.libraries.discovery.ServiceDiscoveryHelper;
import se.tink.libraries.http.client.ServiceClassBuilder;
import se.tink.libraries.http.client.WebResourceFactory;

public class DiscoveredWebServiceClassBuilder implements ServiceClassBuilder {

    private static final InstanceToURLFunction urlGenerator = new InstanceToURLFunction();

    private Client client;
    private ServiceDiscoveryHelper serviceDiscoveryHelper;
    private String serviceName;

    public DiscoveredWebServiceClassBuilder(
            CuratorFramework coordinationClient, Client client, String serviceName) {
        this.serviceDiscoveryHelper = new ServiceDiscoveryHelper(coordinationClient, serviceName);
        this.serviceName = serviceName;
        this.client = client;
    }

    private <T> T build(Class<T> serviceClass, List<RetryableWebResource.Candidate> candidates) {
        ServiceInstance<ServiceDiscoveryHelper.InstanceDetails> instance =
                serviceDiscoveryHelper.queryForInstance();

        String url = "http://" + instance.getAddress() + ":" + instance.getPort() + "/";

        // Can't be reused sinceRetryableWebResource.decorate adds a filter to it.
        WebResource jerseyResource = client.resource(url);

        // Decorate the jerseyResource witth a filter that retries

        RetryableWebResource.decorate(jerseyResource, candidates, serviceName);

        // Create a proxy class that acts as `serviceClass`, but transparently delegates HTTP calls
        // to jerseyResource.

        return WebResourceFactory.newResource(serviceClass, jerseyResource);
    }

    @Override
    public <T> T build(Class<T> serviceClass) {

        // Build a shuffled list of candidates.

        ArrayList<RetryableWebResource.Candidate> candidates = Lists.newArrayList(getCandidates());
        Collections.shuffle(candidates);

        // Build.

        return build(serviceClass, candidates);
    }

    private Iterable<RetryableWebResource.Candidate> getCandidates() {
        return Iterables.concat(
                Iterables.transform(serviceDiscoveryHelper.queryForInstances(), urlGenerator));
    }

    @Override
    public <T> T build(Class<T> serviceClass, Object hashSource) {
        // FIXME: do we need this?
        return null;
    }
}
